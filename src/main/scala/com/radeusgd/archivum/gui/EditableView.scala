package com.radeusgd.archivum.gui

import javafx.beans.value.ObservableBooleanValue
import javafx.collections.{FXCollections, ObservableList}

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.controls.{BoundControl, ChoiceControlFactory, SimpleIntegerFactory, SimpleTextFactory}
import com.radeusgd.archivum.gui.layout._
import com.radeusgd.archivum.persistence.Repository

import scala.xml.XML
import scalafx.beans.Observable
import scalafx.collections.ObservableBuffer
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

   val errors: ObservableBuffer[ValidationError] = new ObservableBuffer[ValidationError]
   val unhandledErrors: ObservableBuffer[ValidationError] = new ObservableBuffer[ValidationError]

   children = root

   var currentRid: Long = -1
   var modelInstance: DMStruct = model.roottype.makeEmpty

   def update(upd: (DMStruct) => DMStruct): Unit = {
      //println("Updating")
      val newInstance = upd(modelInstance)
      val newErrors = model.roottype.validate(newInstance)
      errors.setAll(newErrors:_*)
      val severe = newErrors.exists(_.isInstanceOf[TypeError])
      if (!severe) {
         /*
           warning, we don't call setModelInstance -> refreshBinding,
           we assume that the field changed only its own value
           and other fields need not be updated
         */
         modelInstance = newInstance

         val unhandled = boundControls.foldLeft(newErrors)((e,c) => c.refreshErrors(e))
         unhandledErrors.setAll(unhandled : _*)

         if (newErrors.isEmpty) {
            // TODO debounce (if there are multiple changes during 3s or so make only one submission)
            repo.updateRecord(currentRid, modelInstance)
         } else {
            println(newErrors)
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
      SimpleIntegerFactory,
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