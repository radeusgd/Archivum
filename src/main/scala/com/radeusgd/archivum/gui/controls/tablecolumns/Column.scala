package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.BoundControl
import com.radeusgd.archivum.gui.controls.tablecolumns.Column.Cell

object Column {
   type Cell = scalafx.scene.Node with BoundControl
}

abstract class Column {
   def headerName: String

   // TODO tweak args
   def createControl(path: List[String], ev: EditableView): Column.Cell
}

class SimpleColumn(override val headerName: String, make: (List[String], EditableView) => Cell) extends Column {
   override def createControl(path: List[String], ev: EditableView): Cell = make(path, ev)
}