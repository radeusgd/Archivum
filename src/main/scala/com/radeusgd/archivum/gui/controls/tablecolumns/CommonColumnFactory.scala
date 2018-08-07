package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.BoundControl
import com.radeusgd.archivum.gui.controls.commonproperties.CommonProperties
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.gui.utils.XMLUtils
import scalafx.scene

import scala.xml.Node


class CommonColumnFactory(override val nodeType: String,
                          make: (CommonProperties, List[String], EditableView) => scene.Node with BoundControl)
   extends ColumnFactory {
   override def fromXML(xmlnode: Node, ev: EditableView): Either[LayoutParseError, Column] =
      if (xmlnode.child != Nil) Left(LayoutParseError(xmlnode, "This node shouldn't have any children"))
      else {
         val path = XMLUtils.extractPath(xmlnode).getOrElse(Nil)
         for {
            properties <- CommonProperties.parseXML(xmlnode)
            moddedProps = properties.copy(label="")
         } yield new SimpleColumn(properties.label, (basePath: List[String], ev: EditableView) => make(moddedProps, basePath ++ path, ev))
      }
}
