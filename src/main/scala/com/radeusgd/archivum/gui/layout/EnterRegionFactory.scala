package com.radeusgd.archivum.gui.layout
import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node

object EnterRegionFactory extends LayoutFactory {
  override def fromXML(xmlnode: Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout] = {
    val children = XMLUtils.properChildren(xmlnode)
    if (children.length != 1) {
      Left(LayoutParseError("Enter node has to have exactly one child"))
    } else {
      for {
        subfield <- xmlnode.attribute(ViewLanguage.Field).map(_.text).toRight(LayoutParseError("Enter missing field attribute"))
        child <- EditableView.parseViewTree(children.head, ev, prefix ++ DMUtils.parsePath(subfield))
      } yield child
    }
  }

  override val nodeType: String = "enter"
}
