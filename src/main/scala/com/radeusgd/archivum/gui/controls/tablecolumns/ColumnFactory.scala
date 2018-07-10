package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.layout.LayoutParseError

trait ColumnFactory {
   // TODO it seems this doesn't guarantee us enugh safety
   def fromXML(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, Column]

   val nodeType: String
}
