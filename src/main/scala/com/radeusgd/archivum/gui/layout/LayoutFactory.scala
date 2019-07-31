package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.EditableView

trait LayoutFactory {
   def fromXML(xmlnode: xml.Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout]

   val nodeType: String
}
