package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.DMValue
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.control.TableColumn

case class ResultColumn(name: String, getter: DMValue => String)

class ResultsDisplayColumnFactory[T <: SearchRow](val columns: Seq[ResultColumn]) {
   def makeColumns: List[TableColumn[T, String]] =
      makeIdColumn :: makeGeneratedColumns

   private def makeIdColumn: TableColumn[T, String] =
      new TableColumn[T, String]() {
            prefWidth = 50
            text = "Id"
            cellValueFactory = row => ReadOnlyStringWrapper(row.value.humanId.toString)
         }

   private def makeGeneratedColumns: List[TableColumn[T, String]] =
      for (column <- columns.toList)
         yield new TableColumn[T, String]() {
            prefWidth = 100
            text = column.name
            cellValueFactory = row => ReadOnlyStringWrapper(column.getter(row.value.record))
         }
}
