package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.{DMStruct, Model}
import com.radeusgd.archivum.gui.controls.{BoundControl, SimpleTextFactory}

import scala.collection.mutable
import scala.xml.XML
import scalafx.scene
import scalafx.scene.layout.Pane

class EditableView(val model: Model, xmlroot: xml.Node) extends Pane {
   private def initChildren(xmlnode: xml.Node): (scalafx.scene.Node, Seq[BoundControl]) = {
      val res = EditableView.parseViewTree(xmlnode, this).toTry.get
      (res.node, res.boundControls)
   }

   private val (root, boundControls) = initChildren(xmlroot)

   children = root

   var modelInstance: DMStruct = model.roottype.makeEmpty

   def update(upd: (DMStruct) => DMStruct): Unit = {
      println("Updating")
      val newInstance = upd(modelInstance)
      val errors = model.roottype.validate(newInstance)
      if (errors.isEmpty) {
         // TODO submit to Repo
      } else {
         //TODO display errors
      }
   }

   def setModelInstance(v: DMStruct): Unit = {
      assert(model.roottype.validate(v).isEmpty)
      modelInstance = v
      boundControls.foreach(_.refreshBinding(v))
   }
}

case class ParsedView(node: scene.Node, boundControls: Seq[BoundControl])

trait ViewFactory {
   def fromXML(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView]

   val nodeType: String
}

// TODO maybe make it checked?
case class ViewParseError(message: String) extends RuntimeException(message)

object EditableView {
   def makeFromDefinition(model: Model, text: String): EditableView =
      new EditableView(model, XML.loadString(text))

   private val parsersList: Seq[ViewFactory] = Seq(
      HBoxFactory,
      VBoxFactory,
      LabelFactory,
      SimpleTextFactory
   )

   private val parsers: Map[String, ViewFactory] =
      Map(parsersList map { p: ViewFactory => (p.nodeType, p) }: _*)

   def parseViewTree(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView] = {
      parsers.get(xmlnode.label.toLowerCase).
         toRight(ViewParseError("Unsupported node type '" + xmlnode.label + "'")).
         flatMap(_.fromXML(xmlnode, ev))
   }
}