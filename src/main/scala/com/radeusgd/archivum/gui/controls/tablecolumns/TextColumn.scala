package com.radeusgd.archivum.gui.controls.tablecolumns
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.{ChoiceControl, TableControl, TextControl}
import com.radeusgd.archivum.gui.controls.tablecolumns.Column.Cell
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node

/*class TextColumn(label: String) extends Column {
   override def headerName: String = label

   override def createControl(path: List[String], ev: EditableView): Cell = ???
}*/

object TextColumnFactory
   extends SimpleColumnFactory(ViewLanguage.TextColumn, new TextControl(_, _, _))

object ChoiceColumnFactory
   extends SimpleColumnFactory(ViewLanguage.ChoiceColumn, new ChoiceControl(_, _, _))