package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls.TableControl
import com.radeusgd.archivum.gui.layout.LayoutParseError

trait ColumnFactory {
   def fromXML(xmlnode: xml.Node, tableControl: TableControl): Either[LayoutParseError, Column]

   val nodeType: String
}
