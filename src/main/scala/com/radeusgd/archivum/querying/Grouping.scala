package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.DMValue

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
