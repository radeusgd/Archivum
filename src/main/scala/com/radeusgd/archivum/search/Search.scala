package com.radeusgd.archivum.search

import com.radeusgd.archivum.gui.scenes.EditRecords
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}

class Search(val repository: Repository, val parentEV: EditRecords) extends Scene {

   private val layoutXml = IO.readFileString("search/" + repository.model.name + ".xml")

   private val searchDefinition = SearchDefinition.parseXML(layoutXml)

   private val searchResults: TableView[SearchRow] = new TableView[SearchRow]() {
      columns ++= new ResultsDisplay(searchDefinition.columns).makeColumns.map(TableColumn.sfxTableColumn2jfx)
   }

   private val doSearchButton = new Button("Wyszukaj") {
      onAction = handle {
         ???
      }
   }

   private val foundLabel = new Label()

   private val searchCriteria = searchDefinition.criteriaNode

   private val rootPane = new ScrollPane()
   rootPane.padding = Insets(10)

   rootPane.content = new VBox(10,
      searchCriteria,
      new HBox(doSearchButton, foundLabel),
      searchResults
   )

   root = rootPane



   def refreshSearch(): Unit = {
      // TODO
   }
}
