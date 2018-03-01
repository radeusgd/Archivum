package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.{EditableView, ParsedView, ViewFactory, ViewParseError}
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.scene

abstract class SimpleControlFactory(make: (String, List[String], EditableView) => scene.Node with BoundControl) extends ViewFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView] = {
      if (xmlnode.child != Nil) Left(ViewParseError("This node shouldn't have any children"))
      else {
         val label: Option[String] = xmlnode.attribute(ViewLanguage.Label).map(_.text)
         val makeNode: Option[() => scene.Node with BoundControl] =
            for {
               pathAttr <- xmlnode.attribute(ViewLanguage.BindingPath)
               path <- pathAttr.headOption.map(n => DMUtils.parsePath(n.text))
               if path.nonEmpty
            } yield () => make(label.getOrElse(path.last), path, ev)
         val mkNodeEither = makeNode.toRight(ViewParseError("Missing path attribute")) // convert Option to Either
         val constructed = mkNodeEither.
            flatMap(maker => leftMap(util.Try(maker()).toEither, (t: Throwable) => ViewParseError("Error in constructor", t))) // evaluate Maker and catch and pack any exception to Either
         constructed.map(n => ParsedView(n, Seq(n))) // convert Node to proper result
      }
   }

   //TODO use Cats or scalaz
   private def leftMap[A, B, C](e: Either[A, B], f: A => C): Either[C, B] = {
      e.swap.map(f).swap
   }
}
