package com.radeusgd.archivum.search

import com.radeusgd.archivum.gui.scenes.EditRecords
import scalafx.scene.control.{TableColumn, TableRow, TableView}
import javafx.scene.input.MouseEvent

class SearchResultTable[T <: SearchRow](cols: Seq[TableColumn[T, String]], parentEV: => EditRecords) extends TableView[T] {
   rowFactory = _ => {
      val row = new TableRow[T]()
      row.onMouseClicked = (me: MouseEvent) => {
         if (!row.isEmpty && me.getClickCount == 2) {
            val clickedRid = row.item.value.rid
            parentEV.setModelInstance(clickedRid)
         }
      }
      row
   }
   columns ++= cols.map(TableColumn.sfxTableColumn2jfx)
}
