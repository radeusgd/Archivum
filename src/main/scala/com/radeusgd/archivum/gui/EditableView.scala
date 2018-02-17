package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.gui.controls.SimpleTextFactory

import scala.xml.XML
import scalafx.scene
import scalafx.scene.layout.Pane

class EditableView(root: scalafx.scene.Node) extends Pane {
   children = root

   def update(upd: (DMValue) => DMValue): Unit = {} // TODO
}

trait ViewFactory {
   def fromXML(xmlnode: xml.Node): Either[ViewParseError, scene.Node]
   val nodeType: String
}

// TODO maybe make it checked?
case class ViewParseError(message: String) extends RuntimeException(message)

object EditableView {
   def makeFromDefinition(text: String): EditableView = {
      val xml = XML.loadString(text)
      val root = parseViewTree(xml).toTry
      new EditableView(root.get)
   }

   private val parsersList: Seq[ViewFactory] = Seq(
      HBoxFactory,
      VBoxFactory,
      LabelFactory,
      SimpleTextFactory
   )

   private val parsers: Map[String, ViewFactory] =
      Map(parsersList map { p: ViewFactory => (p.nodeType, p) } :_*)

   def parseViewTree(xmlnode: xml.Node): Either[ViewParseError, scene.Node] = {
      parsers.get(xmlnode.label.toLowerCase).toRight(ViewParseError("Unsupported node type '"+xmlnode.label+"'")).flatMap(_.fromXML(xmlnode))
   }
}