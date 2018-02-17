package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.{EditableView, ParsedView, ViewFactory, ViewParseError}
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.scene

abstract class SimpleControlFactory(make: (String, List[String], EditableView) => scene.Node with BoundControl) extends ViewFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView] = {
      if (xmlnode.child != Nil) Left(ViewParseError("This node shouldn't have any children"))
      else {
         val node = for {
            labelSeq <- xmlnode.attribute(ViewLanguage.Label)
            pathSeq <- xmlnode.attribute(ViewLanguage.BindingPath)
            label <- labelSeq.headOption
            path <- pathSeq.headOption
            if label.isAtom && path.isAtom
         } yield make(label.text, DMUtils.parsePath(path.text), ev)
         node.toRight(ViewParseError("Missing label or path attribute")).map(n => ParsedView(n, Seq(n)))
      }
   }
}
