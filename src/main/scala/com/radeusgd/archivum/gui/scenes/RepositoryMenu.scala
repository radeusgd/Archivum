package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class RepositoryMenu(val repository: Repository) extends Scene {
   lazy val editRecords: EditRecords = new EditRecords(repository, this)
   lazy val exporter: ExportRepository = new ExportRepository(repository, this)
   lazy val importer: ImportRepository = new ImportRepository(repository, this)
   lazy val queries: RunQueries= new RunQueries(repository, this)

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
      utils.makeGoToButton("Queries", queries),
      utils.makeGoToButton("Export", exporter),
      utils.makeGoToButton("Import", importer)
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}
