package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.controls._
import com.radeusgd.archivum.gui.layout._
import com.radeusgd.archivum.persistence.Repository

import scala.xml.XML
import scalafx.collections.ObservableBuffer
import scalafx.scene.layout.Pane

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

   def update(upd: (DMStruct) => DMAggregate): Unit = { // TODO come back to DMStruct, for now a hack
      //println("Updating")
      val newInstance = upd(modelInstance).asInstanceOf[DMStruct] // FIXME I'm so sorry to write shit like this, but I'm in hurry :(
      val newErrors = model.roottype.validate(newInstance)
      val severe = newErrors.exists(_.isInstanceOf[TypeError])
      if (!severe) {
         /*
           warning, we don't call setModelInstance -> refreshBinding,
           we assume that the field changed only its own value
           and other fields need not be updated
         */
         modelInstance = newInstance

         if (newErrors.isEmpty) {
            // TODO debounce (if there are multiple changes during 3s or so make only one submission)
            repo.updateRecord(currentRid, modelInstance)
         } else {
            println(newErrors)
            //TODO display errors
         }
      }

      val unhandled = boundControls.foldLeft(newErrors)((e, c) => c.refreshErrors(e))
      unhandledErrors.setAll(unhandled: _*)
      errors.setAll(newErrors: _*)
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
      TextControlFactory,
      IntegerControlFactory,
      ChoiceControlFactory,
      DateControlFactory,
      ClassicDateControlFactory,
      TableControlFactory
   )

   private val parsers: Map[String, LayoutFactory] =
      (parsersList map { p => (p.nodeType, p) }).toMap

   def parseViewTree(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] =
      parsers.get(xmlnode.label.toLowerCase).
         toRight(LayoutParseError("Unsupported node type '" + xmlnode.label + "'")).
         flatMap(_.fromXML(xmlnode, ev))
}