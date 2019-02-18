package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.DMValue
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.control.TableColumn

case class ResultColumn(name: String, getter: DMValue => String)

class ResultsDisplay(val columns: Seq[ResultColumn]) {
   def makeColumns: Seq[TableColumn[SearchRow, String]] =
      makeIdColumn :: makeGeneratedColumns

   private def makeIdColumn: TableColumn[SearchRow, String] =
      new TableColumn[SearchRow, String]() {
            prefWidth = 50
            text = "Id"
            cellValueFactory = row => ReadOnlyStringWrapper(row.value.humanId.toString)
         }

   private def makeGeneratedColumns: List[TableColumn[SearchRow, String]] =
      for (column <- columns.toList)
         yield new TableColumn[SearchRow, String]() {
            prefWidth = 100
            text = column.name
            cellValueFactory = row => ReadOnlyStringWrapper(column.getter(row.value.record))
         }
}
