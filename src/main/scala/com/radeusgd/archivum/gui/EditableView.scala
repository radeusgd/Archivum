package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.controls.{BoundControl, ChoiceControlFactory, SimpleTextFactory}

import scala.collection.mutable
import scala.xml.XML
import scalafx.scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}
import scalafx.scene.paint.Paint

class EditableView(val model: Model, xmlroot: xml.Node) extends Pane {
   private def initChildren(xmlnode: xml.Node): (scalafx.scene.Node, Seq[BoundControl]) = {
      val res = EditableView.parseViewTree(xmlnode, this).toTry.get
      (res.node, res.boundControls)
   }

   private val (root, boundControls) = initChildren(xmlroot)

   private val errorsLabel = new Label()

   children = new VBox(
      root,
      errorsLabel
   )

   var modelInstance: DMStruct = model.roottype.makeEmpty

   def updateErrorState(errors: Seq[ValidationError]): Unit = {
      if (errors.isEmpty) {
         errorsLabel.text = "OK"
         errorsLabel.textFill.set(Paint.valueOf("green"))
      } else {
         val texts = errors map {
            case ConstraintError(path, message) => path.mkString(".") + ": " + message
            case t : TypeError => t.path.mkString(".") + ": " + t.toString
         }
         errorsLabel.text = texts.mkString("\n")
         errorsLabel.textFill.set(Paint.valueOf("red"))
      }
   }

   def update(upd: (DMStruct) => DMStruct): Unit = {
      //println("Updating")
      val newInstance = upd(modelInstance)
      val errors = model.roottype.validate(newInstance)
      updateErrorState(errors)
      val severe = errors.exists(_.isInstanceOf[TypeError])
      if (severe) return {}
      /*
       warning, we don't call setModelInstance -> refreshBinding,
        we assume that the field changed only its own value
        and other fields need not be updated
       */
      modelInstance = newInstance

      if (errors.isEmpty) {
         // TODO submit to Repo
      } else {
         println(errors)
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
case class ViewParseError(message: String, cause: Throwable = null) extends RuntimeException(message)

object EditableView {
   def makeFromDefinition(model: Model, text: String): EditableView =
      new EditableView(model, XML.loadString(text))

   private val parsersList: Seq[ViewFactory] = Seq(
      HBoxFactory,
      VBoxFactory,
      LabelFactory,
      SimpleTextFactory,
      ChoiceControlFactory
   )

   private val parsers: Map[String, ViewFactory] =
      Map(parsersList map { p: ViewFactory => (p.nodeType, p) }: _*)

   def parseViewTree(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView] = {
      parsers.get(xmlnode.label.toLowerCase).
         toRight(ViewParseError("Unsupported node type '" + xmlnode.label + "'")).
         flatMap(_.fromXML(xmlnode, ev))
   }
}