package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.DMValue

case class ResultCell(key: String, value: DMValue)

case class ResultRow(cells: List[ResultCell]) {
   def columnNames: List[String] = cells.map(_.key)
   def extend(columnName: String, value: DMValue): ResultRow = {
      assert(!columnNames.contains(columnName))
      ResultRow(ResultCell(columnName, value) :: cells)
   }
}

object ResultRow {
   def empty: ResultRow = ResultRow(Nil)
}