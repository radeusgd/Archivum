package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.ApplicationMain
import com.radeusgd.archivum.persistence.Repository

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.layout.VBox

class RepositoryMenu(val repository: Repository) extends Scene {
   lazy val editRecords: EditRecords = new EditRecords(repository, this)

   content = new VBox(
      new Label(repository.model.name) {
         font = scalafx.scene.text.Font(font.name, 25)
      },
      new Button("Edit records") {
         onAction = handle {
            ApplicationMain.switchScene(editRecords)
         }
      },
      new Button("Search") {
         onAction = handle {
            new Alert(AlertType.Warning) {
               title = "Error"
               headerText = "Feature unavailable."
               contentText = "This hasn't been implemented yet."
            }.showAndWait()
         }
      },
      new Button("Queries") {
         onAction = handle {
            new Alert(AlertType.Warning) {
               title = "Error"
               headerText = "Feature unavailable."
               contentText = "This hasn't been implemented yet."
            }.showAndWait()
         }
      },
      new Button("Export") {
         onAction = handle {
            new Alert(AlertType.Warning) {
               title = "Error"
               headerText = "Feature unavailable."
               contentText = "This hasn't been implemented yet."
            }.showAndWait()
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}
