package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMArray, DMStruct, DMUtils, DMValue}

import scala.math.Ordering

case class AppendPrefix(columnName: String, mapping: DMValue => DMValue)

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

   def groupBy[A](path: String, appendPrefixOpt: Option[AppendPrefix], sortBy: DMValue => A)(implicit ord: Ordering[A]): Seq[MultipleResultRow] = {
      def makeGroups(): Map[DMValue, Seq[DMValue]] = {
         val getter = DMUtils.makeGetter(path)
         val groups = collection.mutable.Map.empty[DMValue, List[DMValue]]
         objects.foreach(obj => {
            val groupName = getter(obj)
            val tail = groups.getOrElse(groupName, Nil)
            groups(groupName) = obj :: tail
         })
         groups.toMap
      }

      def alterPrefix(groupName: DMValue): ResultRow = {
         appendPrefixOpt.map(appendPrefix => prefix.extend(appendPrefix.columnName, appendPrefix.mapping(groupName))).getOrElse(prefix)
      }

      val groups = makeGroups()
      val newRows = groups.toList.map({ case (groupName, elems) =>
         MultipleResultRow(alterPrefix(groupName), elems)
      })

      newRows.map(_.sortBy(sortBy))
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