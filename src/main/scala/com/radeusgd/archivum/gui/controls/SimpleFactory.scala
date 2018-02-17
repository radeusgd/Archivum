package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.{ViewFactory, ViewParseError}
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.scene

abstract class SimpleFactory(make: (String, Seq[String]) => scene.Node) extends ViewFactory {
   override def fromXML(xmlnode: xml.Node): Either[ViewParseError, scene.Node] = {
      if (xmlnode.child != Nil) Left(ViewParseError("This node shouldn't have any children"))
      else {
         val node = for {
            labelSeq <- xmlnode.attribute(ViewLanguage.Label)
            pathSeq <- xmlnode.attribute(ViewLanguage.BindingPath)
            label <- labelSeq.headOption
            path <- pathSeq.headOption
            if label.isAtom && path.isAtom
         } yield make(label.text, DMUtils.parsePath(path.text))
         node.toRight(ViewParseError("Missing label or path attribute"))
      }
   }
}
