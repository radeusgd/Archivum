package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._

sealed abstract class SortingOrder
object Ascending extends SortingOrder
object Descending extends SortingOrder

sealed abstract class GroupingSortType
case class CanonicalSorting(order: SortingOrder) extends GroupingSortType
case class PopularitySorted(order: SortingOrder) extends GroupingSortType

sealed abstract class AppendColumnMode
object DoNotAppend extends AppendColumnMode
object Default extends AppendColumnMode // uses path as columnName and identity mapping
case class CustomAppendColumn(columnName: String, mapping: DMValue => DMValue = identity) extends AppendColumnMode

sealed abstract class Grouping(val appendColumnMode: AppendColumnMode) {
   def defaultColumnName: String

   //noinspection ScalaStyle
   def groupDMs(objects: Seq[DMValue]): Seq[(DMValue, Seq[DMValue])] = {
      val grouping = this
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
            groups.updated(DMString("Razem"), objects).toList
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
}

object Grouping {
   def groupkeyToString(k: DMValue): String = k.toString // TODO maybe something better than toString ?
}

case class GroupBy(
                     path: String,
                     sortType: GroupingSortType = CanonicalSorting(Ascending),
                     override val appendColumnMode: AppendColumnMode = Default)
   extends Grouping(appendColumnMode) {
   override def defaultColumnName: String = path // TODO maybe convert dots to something human readable ?
}
case class GroupByYears(datePath: String, yearInterval: Int, override val appendColumnMode: AppendColumnMode = Default) extends Grouping(appendColumnMode) {
   override def defaultColumnName: String = datePath
}
case class GroupByWithSummary(path: String) extends Grouping(Default) {
   override def defaultColumnName: String = path
}
case class CustomGroupBy[A](path: String,
                            mapping: DMValue => DMValue,
                            orderMapping: DMValue => A,
                            filter: DMValue => Boolean = _ => true,
                            override val appendColumnMode: AppendColumnMode = Default)(implicit ordering: Ordering[A]) extends Grouping(appendColumnMode) {
   override def defaultColumnName: String = path
   def ord: Ordering[A] = ordering
}

object CustomGroupBy {
   private val monthNames = Array(
      "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"
   )

   def groupByMonth(path: String, appendColumnMode: AppendColumnMode = Default): CustomGroupBy[Int] = {
      CustomGroupBy(path,
         filter = {
            case _: DMDate => true
            case DMYearDate(Right(_)) => true
            case _ => false
         },
         mapping = {
            case DMDate(date) => DMString(monthNames(date.getMonthValue - 1))
            case DMYearDate(Right(date)) => DMString(monthNames(date.getMonthValue - 1))
         },
         orderMapping = {
            case DMDate(date) => date.getMonthValue
            case DMYearDate(Right(date)) => date.getMonthValue
         }
      )
   }
}
