package com.radeusgd.archivum.gui.controls.commonproperties

import cats.implicits._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.BoundControl
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import scalafx.scene

import scala.util.Try
// TODO
abstract class CommonControlFactory(make: (CommonProperties, List[String], EditableView) => scene.Node with BoundControl) extends LayoutFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] = {
      if (xmlnode.child != Nil) Left(LayoutParseError("This node shouldn't have any children"))
      else {
         for {
            path <- XMLUtils.extractPath(xmlnode)
            properties <- CommonProperties.parseXML(xmlnode)
            node <- safeConstruct(path, properties, ev)
         } yield ParsedLayout(node, Seq(node))
      }
   }


   private def safeConstruct(path: List[String], props: CommonProperties, ev: EditableView): Either[LayoutParseError, scene.Node with BoundControl] =
      Try(make(props, path, ev))
         .toEither
         .leftMap((t: Throwable) => LayoutParseError("Error in constructor" + path, Some(t)))
}
