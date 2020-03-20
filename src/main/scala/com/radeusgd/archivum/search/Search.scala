package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.{DMString, DMStruct, DMUtils}
import com.radeusgd.archivum.gui.scenes.EditRecords
import com.radeusgd.archivum.persistence._
import com.radeusgd.archivum.utils.IO
import scalafx.Includes._
import javafx.concurrent.Task
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.{Cursor, Scene}
import scalafx.scene.control._
import scalafx.scene.layout._

class Search(val repository: Repository, parentEVF: => EditRecords) extends Scene {
   lazy val parentEV: EditRecords = parentEVF

   private val layoutXml = IO.readFileString("Konfiguracja/search/" + repository.model.name + ".xml")

   private val searchDefinition = SearchDefinition.parseXML(layoutXml)
   private val advancedSearch = new AdvancedSearchDefinition

   private val searchResults: TableView[SearchRow] =
      new SearchResultTable[SearchRow](new ResultsDisplayColumnFactory[SearchRow](searchDefinition.columns).makeColumns, parentEV)

   private val foundLabel = new Label()

   private def setTextInfo(str: String): Unit = Platform.runLater {
      foundLabel.text = str
   }

   private def makeSearchTask(): Task[Unit] = new Task[Unit]() {
      override def call(): Unit = {
         setTextInfo("Wyszukiwanie w toku...")
         val conditions: Seq[SearchCondition] =
            searchDefinition.conditions.flatMap(_.getCurrentCondition()) ++ advancedSearch.getConditions
         if (conditions.isEmpty) {
            setTextInfo("Nie podano żadnych kryteriów")
         } else {
            val (fulltext: Option[String], filter: SearchCriteria) = conditions.foldLeft[(Option[String], SearchCriteria)]((None, Truth)) {
               case ((_, sca), FulltextMatch(txt)) => (Some(txt), sca)  // I assume only one fulltext node is present
               case ((fta, sca), ExactMatch(path, value)) =>
                  repository.model
                  (fta, And(sca, Equal(DMUtils.parsePath(path), DMString(value)))) // TODO I assume equality is on string types only!!!
               // TODO to fix this we need to inspect model type using repository.model.rootType
               case ((fta, sca), YearDateMatch(path, year)) =>
                  (fta, And(sca, HasPrefix(DMUtils.parsePath(path), f"$year%04d")))
               // this is an awful reliance on implementation details from other module, but types and database are already tightly coupled
            }

            val results: Seq[(repository.Rid, DMStruct)] = fulltext match {
               case Some(ft) => repository.fullTextSearch(ft, filter, caseSensitivityCheckBox.selected.value)
               case None => repository.searchRecords(filter)
            }

            val maxResLen = 1500

            val resultsTruncated = if (results.length > maxResLen) {
               setTextInfo(s"Znaleziono ${results.length} wyników. Wyświetlam pierwsze $maxResLen")
               results.take(maxResLen)
            } else {
               setTextInfo(s"Znaleziono ${results.length} wyników.")
               results
            }

            val newTableContent: Seq[SearchRow] = for {
               (rid, dms) <- resultsTruncated
            } yield new SearchRow(rid, () => repository.ridSet.getTemporaryIndex(rid).toInt, dms)

            Platform.runLater {
               searchResults.items.get().setAll(newTableContent:_*)
            }
         }
      }
   }

   private def setComputeInProgress(isComputing: Boolean): Unit = Platform.runLater {
      this.setCursor(if (isComputing) Cursor.Wait else Cursor.Default)
   }

   private val caseSensitivityCheckBox = new CheckBox("Rozróżniaj małe i wielkie litery w ogólnym wyszukiwaniu")
   caseSensitivityCheckBox.indeterminate = false
   caseSensitivityCheckBox.selected = true

   private val doSearchButton = new Button("Wyszukaj") {
      onAction = handle {
         val task = makeSearchTask()
         task.onSucceeded = _ => {
            setComputeInProgress(false)
         }
         task.onFailed = _ => {
            setComputeInProgress(false)
         }
         setComputeInProgress(true)
         new Thread(task).start()
      }
   }

   private val searchCriteria = searchDefinition.criteriaNode

   private val rootPane = new ScrollPane()
   rootPane.padding = Insets(10)

   rootPane.content = new VBox(10,
      searchCriteria,
      caseSensitivityCheckBox,
      new HBox(10, doSearchButton, foundLabel),
      searchResults
   )

   searchResults.hgrow = Priority.Always
   searchResults.vgrow = Priority.Always
   rootPane.vgrow = Priority.Always

   root = rootPane
}
