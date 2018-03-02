package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView, Refreshable, utils}
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{BorderPane, HBox, VBox}

class EditRecords(val repository: Repository, val parentScene: Scene) extends Scene with Refreshable {

   private def model: Model = repository.model

   private val layoutXml = IO.readFile("views/" + model.name + ".xml")

   private val editableView: EditableView = EditableView.makeFromDefinition(repository, layoutXml)

   root = new BorderPane {
      padding = Insets(10)
      top = new HBox(
         utils.makeGoToButton("< Back", parentScene)
      )
      center = editableView
      bottom = new HBox(
         new Button("TODO"),
         new Button("TODO")
      )
   }

   override def refresh(): Unit = {
      /*
      Stupid workaround, because BorderPane's bottom doesn't show unless window is resized
       */
      ApplicationMain.stage.height = ApplicationMain.stage.height.value + 0.1
   }
}