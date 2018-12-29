package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._

import scala.collection.immutable
import scala.math.Ordering

case class MultipleResultRow(prefix: ResultRow, objects: Seq[DMValue]) {
   def values(path: String): Seq[DMValue] =
      objects.map(DMUtils.makeGetter(path))

   /*
   First steps towards array support.
   Maps every contained object into a list of objects contained in its array.
   Unfortunately it discards all other properties of that object,
   so one cannot access arrays and non-arrays at the same time at one level of grouping.
    */
   def unpackArray(path: String): MultipleResultRow = {
      val getter = DMUtils.makeGetter(path)
      def accessArray(obj: DMValue): Seq[DMValue] =
         getter(obj).asInstanceOf[DMArray].values
      MultipleResultRow(prefix, objects.flatMap(accessArray))
   }

   /*
   Filters contained objects with given predicate.
   Resulting row may be empty (just a prefix).
    */
   def filter(predicate: DMValue => Boolean): MultipleResultRow =
      MultipleResultRow(prefix, objects.filter(predicate))

   def sortBy[B](f: DMValue => B)(implicit ord: Ordering[B]): MultipleResultRow =
      MultipleResultRow(prefix, objects.sortBy(f))

   def groupBy(grouping: Grouping): Seq[MultipleResultRow] = {
      def alterPrefix(groupName: DMValue): ResultRow = {
         grouping.appendColumnMode match {
            case DoNotAppend => prefix
            case Default => prefix.extend(grouping.defaultColumnName, groupName)
            case CustomAppendColumn(columnName, mapping) =>
               prefix.extend(columnName, mapping(groupName))
         }
      }

      def extractYearFromDateDM(v: DMValue): Int = v match {
         case DMDate(value) => value.getYear
         case yd: DMYearDate => yd.year
         case _ => throw new RuntimeException("Trying to extract year from non-date value")
      }

      val groups: Seq[(DMValue, Seq[DMValue])] = grouping match {
         case GroupBy(path, _, _) =>
            objects.groupBy(DMUtils.makeGetter(path)).toList
         case GroupByYears(datePath, yearInterval, _) =>
            val getter = DMUtils.makeGetter(datePath)
            val filtered = objects.filter(root => getter(root) != DMNull) // when we group by year, we drop all records that have it missing
            val grouped = filtered.groupBy(root => extractYearFromDateDM(getter(root))).toList
            grouped.map(t => (DMInteger(t._1), t._2))
         case GroupByWithSummary(path) =>
            val groups = objects.groupBy(DMUtils.makeGetter(path))
            groups.updated(DMString("ALL"), objects).toList
      }

      def ascendingToOrder[A](order: SortingOrder)(list: Seq[A]): Seq[A] = order match {
         case Ascending => list
         case Descending => list.reverse
      }

      val sorted = grouping match {
         case GroupBy(_, sortType, _) => sortType match {
            // TODO what about canonical sorting on DMValues that are not DMOrdered? at least try giving a better error message
            case CanonicalSorting(order) => ascendingToOrder(order)(groups.sortBy(_._1.asInstanceOf[DMOrdered]))
            case PopularitySorted(order) => ascendingToOrder(order)(groups.sortBy(_._2.length))
         }
         case GroupByYears(_, _, _) => groups.sortBy(_._1.asInstanceOf[DMOrdered])
         case _: GroupByWithSummary => groups
      }

      sorted.map({ case (groupName, elems) =>
         MultipleResultRow(alterPrefix(groupName), elems)
      })
   }

   def aggregate(aggregations: Seq[(String, Seq[DMValue] => DMValue)]): ResultRow = {
      aggregations.foldLeft(prefix){
         case (agg: ResultRow, (name, f)) => agg.extend(name, f(objects))
      }
   }
}

object MultipleResultRow {
   def apply(prefix: ResultRow, objects: Seq[DMValue]): MultipleResultRow = new MultipleResultRow(prefix, objects)
   def apply(objects: Seq[DMValue]): MultipleResultRow = MultipleResultRow(ResultRow.empty, objects)
}