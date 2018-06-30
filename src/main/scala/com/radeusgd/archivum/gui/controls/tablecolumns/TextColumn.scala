package com.radeusgd.archivum.gui.controls.tablecolumns
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.TableControl
import com.radeusgd.archivum.gui.controls.tablecolumns.Column.Cell
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node

class TextColumn(label: String) extends Column {
   override def headerName: String = label

   override def createControl(path: List[String], ev: EditableView): Cell = ???
}

object TextColumnFactory extends ColumnFactory {
   override def fromXML(xmlnode: Node, tableControl: TableControl): Either[LayoutParseError, Column] = ???

   override val nodeType: String = ViewLanguage.TextColumn
}