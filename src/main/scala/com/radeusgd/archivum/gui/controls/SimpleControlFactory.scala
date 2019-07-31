package com.radeusgd.archivum.gui.controls

import cats.implicits._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage

import scala.util.Try
import scalafx.scene

abstract class SimpleControlFactory(make: (String, List[String], EditableView) => scene.Node with BoundControl) extends LayoutFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout] = {
      if (xmlnode.child != Nil) Left(LayoutParseError("This node shouldn't have any children"))
      else {
         val label: Option[String] = xmlnode.attribute(ViewLanguage.Label).map(_.text)
         val path = XMLUtils.extractPath(xmlnode)

         path.flatMap(p => safeConstruct(prefix ++ p, label, ev))
            .map(n => ParsedLayout(n, Seq(n))) // convert Node to proper result
      }
   }

   private def safeConstruct(path: List[String], label: Option[String], ev: EditableView): Either[LayoutParseError, scene.Node with BoundControl] =
      Try(make(label.getOrElse(path.last), path, ev))
         .toEither
         .leftMap((t: Throwable) => LayoutParseError("Error in constructor " + path, Some(t)))
}
