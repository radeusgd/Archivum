package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.controls.{BoundControl, ChoiceControlFactory, SimpleTextFactory}
import com.radeusgd.archivum.gui.layout._
import com.radeusgd.archivum.persistence.Repository

import scala.xml.XML
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}
import scalafx.scene.paint.Paint

class EditableView(val repo: Repository, xmlroot: xml.Node) extends Pane {

   def model: Model = repo.model

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

   var currentRid: Long = -1
   var modelInstance: DMStruct = model.roottype.makeEmpty

   def updateErrorState(errors: Seq[ValidationError]): Unit = {
      if (errors.isEmpty) {
         errorsLabel.text = "OK"
         errorsLabel.textFill.set(Paint.valueOf("green"))
      } else {
         val texts = errors map {
            case ConstraintError(path, message) => path.mkString(".") + ": " + message
            case t: TypeError => t.path.mkString(".") + ": " + t.toString
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
      if (!severe) {
         /*
           warning, we don't call setModelInstance -> refreshBinding,
           we assume that the field changed only its own value
           and other fields need not be updated
         */
         modelInstance = newInstance

         if (errors.isEmpty) {
            // TODO debounce (if there are multiple changes during 3s or so make only one submission)
            repo.updateRecord(currentRid, modelInstance)
         } else {
            println(errors)
            //TODO display errors
         }
      }
   }

   def setModelInstance(rid: Long): Unit = {
      repo.fetchRecord(rid).foreach(v => {
         assert(model.roottype.validate(v).isEmpty)
         currentRid = rid
         modelInstance = v
         boundControls.foreach(_.refreshBinding(v))
      })
   }
}

object EditableView {
   def makeFromDefinition(repo: Repository, text: String): EditableView =
      new EditableView(repo, XML.loadString(text))

   private val parsersList: Seq[LayoutFactory] = Seq(
      HBoxFactory,
      VBoxFactory,
      LabelFactory,
      SimpleTextFactory,
      ChoiceControlFactory
   )

   private val parsers: Map[String, LayoutFactory] =
      Map(parsersList map { p: LayoutFactory => (p.nodeType, p) }: _*)

   def parseViewTree(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] = {
      parsers.get(xmlnode.label.toLowerCase).
         toRight(LayoutParseError("Unsupported node type '" + xmlnode.label + "'")).
         flatMap(_.fromXML(xmlnode, ev))
   }
}