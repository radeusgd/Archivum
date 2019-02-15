package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.{ConstraintError, Model, TypeError}
import com.radeusgd.archivum.gui.controls.LayoutDefaults
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView, Refreshable, utils}
import com.radeusgd.archivum.persistence.DBUtils.Rid
import com.radeusgd.archivum.persistence.{Repository, RidSetHelper}
import com.radeusgd.archivum.utils.IO
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane, TextField}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.paint.Paint
import scalafx.Includes.handle
import scalafx.scene.input.KeyCode

class EditRecords(val repository: Repository, val parentScene: Scene) extends Scene with Refreshable {

   private def model: Model = repository.model

   private val layoutXml = IO.readFileString("views/" + model.name + ".xml")

   private val editableView: EditableView = EditableView.makeFromDefinition(repository, layoutXml)

   private def currentRid: Rid = editableView.currentRid

   private val ridSet: RidSetHelper = repository.ridSet

   private def setSomeModelInstance(hintRid: Rid): Unit = {
      val rid = ridSet.getCloseRid(hintRid)
         .getOrElse(repository.createRecord(model.makeEmpty))
      setModelInstance(rid)
   }

   private def setModelInstance(rid: Rid): Unit = {
      if (editableView.errors.isEmpty || utils.ask("Some changes have not been saved due to errors, do you want to continue?", "These changes will be lost."))
      editableView.setModelInstance(rid)
      indexTextField.text = ridSet.getTemporaryIndex(rid).toString
      countLabel.text = "/" + ridSet.count()
      countLabel.requestFocus()
   }

   private val indexTextField = new TextField {
      prefWidth = 60
      onAction = handle {
         try {
            val ind = text.value.toInt
            val ridOpt = repository.ridSet.findRidForIndex(ind)
            val rid = ridOpt.getOrElse(throw new RuntimeException("No record with such id found"))

            setModelInstance(rid)
         } catch {
            case e: NumberFormatException =>
               utils.showError("Record id has to be a number")
            case e: RuntimeException =>
               utils.showError(e.getMessage)
         }
      }
   }
   private val countLabel = new Label("/0") {
      alignment = Pos.CenterRight // TODO vertical alignment
   }

   private val errorsLabel = new Label
   private val modifiedLabel = new Label("TEST")
   editableView.modifiedStatus = Some(modifiedLabel)

   private def deleteCurrent(): Unit = {
      if (utils.ask("Are you sure you want to delete this entry?")) {
         repository.deleteRecord(currentRid)
         setSomeModelInstance(currentRid)
      }
   }

   private def insertEmpty(): Unit = {
      val rid = repository.createRecord(editableView.makeNewModelInstance())
      setModelInstance(rid)
   }

   root = new BorderPane {
      padding = Insets(10)
      top = new HBox(
         utils.makeGoToButton("< Back", parentScene)
      )
      private val scrollPane = new ScrollPane()
      scrollPane.content = editableView
      center = scrollPane
      bottom = new HBox(LayoutDefaults.defaultSpacing,
         utils.mkButton("<--", () => ridSet.getFirstRid().foreach(setModelInstance)),
         utils.mkButton("<-", () => ridSet.getPreviousRid(currentRid).foreach(setModelInstance)),
         new HBox(indexTextField, countLabel),
         utils.mkButton("->", () => ridSet.getNextRid(currentRid).foreach(setModelInstance)),
         utils.mkButton("-->", () => ridSet.getLastRid().foreach(setModelInstance)),
         utils.mkButton("Delete", deleteCurrent),
         utils.mkButton("Create empty", insertEmpty),
         errorsLabel,
         modifiedLabel
      )
   }

   override def refresh(): Unit = {
      /*
      Stupid workaround, because BorderPane's bottom doesn't show unless window is resized
       */
      ApplicationMain.stage.height = ApplicationMain.stage.height.value + 0.1
   }

   // initialize with some model instance
   setSomeModelInstance(0)

   editableView.errors.onChange {
      val errors = editableView.errors
      if (errors.isEmpty) {
         errorsLabel.text = "OK"
         errorsLabel.textFill.set(Paint.valueOf("green"))
      } else {
         val texts = editableView.unhandledErrors.map(e => e.getPath.mkString(".") + ": " + e.getMessage)
         errorsLabel.text = if (texts.isEmpty) "There are errors" else texts.mkString("\n")
         errorsLabel.textFill.set(Paint.valueOf("red"))
      }
   }

   onKeyPressed = e => {
      if (e.isControlDown && e.getCode == KeyCode.R.delegate) {
         val hotswap = new EditRecords(repository, parentScene)
         ApplicationMain.switchScene(hotswap)
         hotswap.modifiedLabel.text = "View has been reloaded"
         hotswap.refresh()
      }
   }
}