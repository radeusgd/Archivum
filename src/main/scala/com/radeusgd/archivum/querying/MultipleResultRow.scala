package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._

case class MultipleResultRow(prefix: ResultRow, objects: NestedMapADT[String, Seq[DMValue]]) {
   /*
   Filters contained objects with given predicate.
   Resulting row may be empty (just a prefix).
    */
   def filter(predicate: DMValue => Boolean): MultipleResultRow =
      MultipleResultRow(prefix, objects.map(_.filter(predicate)))

   //noinspection ScalaStyle
   private def groupDMs(objects: Seq[DMValue], grouping: Grouping): Seq[(DMValue, Seq[DMValue])] = {
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
         val grouped: Seq[(Int, Seq[DMValue])] = filtered.groupBy(root => extractYearFromDateDM(getter(root)) / yearInterval).toList
            def makeYearRange(int: Int): DMValue =
               if (yearInterval == 1) DMInteger(int)
               else {
                  val start = int * yearInterval
                  val end = start + yearInterval - 1
                  DMString(start + " - " + end)
               }
            grouped.map(t => (makeYearRange(t._1), t._2))
         case GroupByWithSummary(path) =>
            val groups = objects.groupBy(DMUtils.makeGetter(path))
            groups.updated(DMString("ALL"), objects).toList
         case CustomGroupBy(path, mapping, _, filter, _) =>
            val getter = DMUtils.makeGetter(path)
            val filtered = objects.filter(v => filter(getter(v)))
            filtered.groupBy(v => mapping(getter(v))).toList
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
         case customGroupBy: CustomGroupBy[Any] =>
            val getter = DMUtils.makeGetter(customGroupBy.path)
            groups.sortBy(t => customGroupBy.orderMapping(getter(t._2.head)))(customGroupBy.ord)
      }

      sorted
   }

   //noinspection ScalaStyle
   def groupBy(grouping: Grouping): Seq[MultipleResultRow] = {
      def alterPrefix(groupName: DMValue): ResultRow = {
         grouping.appendColumnMode match {
            case DoNotAppend => prefix
            case Default => prefix.updated(grouping.defaultColumnName, groupName)
            case CustomAppendColumn(columnName, mapping) =>
               prefix.updated(columnName, mapping(groupName))
         }
      }

      objects match {
         case NestedMapElement(value) =>
            val sorted = groupDMs(value, grouping)
            sorted.map({ case (groupName, elems) =>
               MultipleResultRow(alterPrefix(groupName), elems)
            })
         case NestedMap(mapping) => ??? // FIXME this is hard but possible, need to gather into groups in each leaf of NestedMap, than merge these groups by the grouping into rows and put them back into NestedMap, filling missing places with empty lists
      }
   }

   def groupByHorizontal(grouping: Grouping): MultipleResultRow = {
      MultipleResultRow(prefix, objects.flatMap(
         (objs: Seq[DMValue]) => {
            groupDMs(objs, grouping).foldLeft[NestedMap[String, Seq[DMValue]]](NestedMap.empty)({
               case (nm, (key, vals)) =>
                  nm.updated(key.toString, vals) // TODO maybe something better than toString ?
            })
      }))
   }

   def aggregate(aggregations: Seq[(String, Seq[DMValue] => DMValue)]): ResultRow = {
      prefix.append(objects.flatMap(
         (objs: Seq[DMValue]) =>
            aggregations.foldLeft[ResultRow](NestedMap.empty){
               case (agg: ResultRow, (name, f)) => agg.updated(name, f(objs))
            }
      ))
   }

   def aggregate(f: Seq[DMValue] => ResultRow): ResultRow = {
      prefix.append(objects.flatMap(f))
   }
}

object MultipleResultRow {
   def apply(prefix: ResultRow, objects: Seq[DMValue]): MultipleResultRow = new MultipleResultRow(prefix, NestedMapElement(objects))
   def apply(objects: Seq[DMValue]): MultipleResultRow = MultipleResultRow(NestedMap.empty[String, DMValue], objects)
}