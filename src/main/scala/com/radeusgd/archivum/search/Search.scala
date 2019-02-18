package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.{DMString, DMStruct, DMUtils}
import com.radeusgd.archivum.gui.scenes.EditRecords
import com.radeusgd.archivum.persistence._
import com.radeusgd.archivum.utils.IO
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._

class Search(val repository: Repository, val parentEV: EditRecords) extends Scene {

   private val layoutXml = IO.readFileString("search/" + repository.model.name + ".xml")

   private val searchDefinition = SearchDefinition.parseXML(layoutXml)

   private val searchResults: TableView[SearchRow] = new TableView[SearchRow]() {
      columns ++= new ResultsDisplay(searchDefinition.columns).makeColumns.map(TableColumn.sfxTableColumn2jfx)
      rowFactory = _ => {
         val row = new TableRow[SearchRow]()
         row.onMouseClicked = (me: MouseEvent) => {
            if (!row.isEmpty && me.clickCount == 2) {
               val clickedRid = row.item.value.rid
               parentEV.setModelInstance(clickedRid)
            }
         }
         row
      }
   }

   private val foundLabel = new Label()

   private val doSearchButton = new Button("Wyszukaj") {
      onAction = handle {
         foundLabel.text = "Wyszukiwanie w toku..."
         // TODO make it run in background!
         val conditions: Seq[SearchCondition] =
            searchDefinition.conditions.flatMap(_.getCurrentCondition())
         if (conditions.isEmpty) {
            foundLabel.text = "Nie podano żadnych kryteriów"
         } else {
            val (fulltext: Option[String], filter: SearchCriteria) = conditions.foldLeft[(Option[String], SearchCriteria)]((None, Truth)) {
               case ((_, sca), FulltextMatch(txt)) => (Some(txt), sca)  // I assume only one fulltext node is present
               case ((fta, sca), ExactMatch(path, value)) =>
                  (fta, And(sca, Equal(DMUtils.parsePath(path), DMString(value)))) // TODO I assume equality is on string types only!!!
                  // TODO to fix this we need to inspect model type using repository.model.rootType
               case ((fta, sca), YearDateMatch(path, year)) =>
                  (fta, And(sca, HasPrefix(DMUtils.parsePath(path), f"$year%04d")))
                  // this is an awful reliance on implementation details from other module, but types and database are already tightly coupled
            }

            val results: Seq[(repository.Rid, DMStruct)] = fulltext match {
               case Some(ft) => repository.fullTextSearch(ft, filter)
               case None => repository.searchRecords(filter)
            }

            val maxResLen = 500

            val resultsTruncated = if (results.length > maxResLen) {
               foundLabel.text = s"Znaleziono ${results.length} wyników. Wyświetlam pierwsze $maxResLen"
               results.take(maxResLen)
            } else {
               foundLabel.text = s"Znaleziono ${results.length} wyników."
               results
            }

            val newTableContent: Seq[SearchRow] = for {
               (rid, dms) <- resultsTruncated
            } yield new SearchRow(rid, () => repository.ridSet.getTemporaryIndex(rid).toInt, dms)

            searchResults.items.get().setAll(newTableContent:_*)
         }
      }
   }

   private val searchCriteria = searchDefinition.criteriaNode

   private val rootPane = new ScrollPane()
   rootPane.padding = Insets(10)

   rootPane.content = new VBox(10,
      searchCriteria,
      new HBox(10, doSearchButton, foundLabel),
      searchResults
   )

   searchResults.hgrow = Priority.Always
   searchResults.vgrow = Priority.Always

   root = rootPane



   def refreshSearch(): Unit = {
      // TODO
   }
}
