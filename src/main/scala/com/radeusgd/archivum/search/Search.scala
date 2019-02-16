package com.radeusgd.archivum.search

import com.radeusgd.archivum.gui.scenes.EditRecords
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO
import scalafx.scene.Scene
import scalafx.scene.control.{TableColumn, TableView}
import scalafx.scene.layout.{HBox, VBox}

class Search(val repository: Repository, val parentEV: EditRecords) extends Scene {

   private val layoutXml = IO.readFileString("search/" + repository.model.name + ".xml")

   private val searchDefinition = SearchDefinition.parseXML(layoutXml)

   private val searchResults: TableView[SearchRow] = new TableView[SearchRow]() {
      columns ++= new ResultsDisplay(searchDefinition.columns).makeColumns.map(TableColumn.sfxTableColumn2jfx)
   }

   private val searchCriteria = HBox(
      // TODO
   )

   content += VBox(
      searchCriteria,
      searchResults
   )

   def refreshSearch(): Unit = {
      // TODO
   }
}
