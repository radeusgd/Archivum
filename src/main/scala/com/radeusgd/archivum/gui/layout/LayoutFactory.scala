package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.EditableView

trait LayoutFactory {
   def fromXML(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, ParsedLayout]

   val nodeType: String
}
