package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.ApplicationMain
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.gui.utils

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class RepositoryMenu(val repository: Repository) extends Scene {
   lazy val editRecords: EditRecords = new EditRecords(repository, this)

   content = new VBox(
      new Label(repository.model.name) {
         font = scalafx.scene.text.Font(font.name, 25)
      },
      utils.makeGoToButtonRefreshable("Edit records", editRecords),
      new Button("Search") {
         onAction = handle {
            utils.notImplemented()
         }
      },
      new Button("Queries") {
         onAction = handle {
            utils.notImplemented()
         }
      },
      new Button("Export") {
         onAction = handle {
            utils.notImplemented()
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}
