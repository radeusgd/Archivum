package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox

class EditRecords(val repository: Repository, val parentScene: Scene) extends Scene {

   private def model: Model = repository.model

   private val layoutXml = IO.readFile("views/" + model.name + ".xml")

   private val editableView: EditableView = EditableView.makeFromDefinition(repository, layoutXml)

   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(parentScene)
            }
         },
         editableView
      )
   }
}