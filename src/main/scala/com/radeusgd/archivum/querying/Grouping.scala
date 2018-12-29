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
case class CustomAppendColumn(columnName: String, mapping: DMValue => DMValue) extends AppendColumnMode

case class Grouping(path: String, sortType: GroupingSortType, appendColumnMode: AppendColumnMode = Default)
