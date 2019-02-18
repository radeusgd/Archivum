package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.{ApplicationMain, utils}
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.search.Search
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.stage.Stage

class RepositoryMenu(val repository: Repository, private val parent: Scene) extends Scene {
   lazy val editRecords: EditRecords = new EditRecords(repository, this)
   lazy val exporter: ExportRepository = new ExportRepository(repository, this)
   lazy val importer: ImportRepository = new ImportRepository(repository, this)
   lazy val queries: RunQueries= new RunQueries(repository, this)

   content = new VBox(
      new Label(repository.model.name) {
         font = scalafx.scene.text.Font(font.name, 25)
      },
      utils.makeGoToButtonRefreshable("Przeglądanie / edycja rekordów", editRecords),
      new Button("Search") {
         onAction = handle {
            // TODO this is quite hacky but personally I'd just get rid of this menu option
            def showER(): EditRecords = {
               ApplicationMain.switchScene(editRecords)
               ApplicationMain.setPrimaryStageVisibility(true)
               editRecords
            }
            val searchScene = new Search(repository, showER())
            val stage2 = new Stage()
            stage2.setTitle("Wyszukiwanie")
            stage2.setScene(searchScene)
            ApplicationMain.setPrimaryStageVisibility(false)
            stage2.show()
         }
      },
      utils.makeGoToButton("Kwerendy", queries),
      utils.makeGoToButton("Eksport", exporter),
      utils.makeGoToButton("Import", importer),
      utils.makeGoToButton("Wróć do wyboru bazy", parent)
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}
