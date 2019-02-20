package com.radeusgd.archivum.search.commonfeatures

import com.radeusgd.archivum.gui.scenes.EditRecords
import com.radeusgd.archivum.persistence.DBUtils.Rid
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.search.{SearchDefinition, SearchResultTable}
import com.radeusgd.archivum.utils.IO
import scalafx.application.Platform
import scalafx.Includes._
import javafx.concurrent.Task
import scalafx.geometry.Insets
import scalafx.scene.{Cursor, Scene}
import scalafx.scene.control.{Label, ScrollPane}
import scalafx.scene.layout.VBox

class CommonFeaturesScene(val baseRid: Rid, val repository: Repository, parentEVF: => EditRecords) extends Scene {

   lazy val parentEV: EditRecords = parentEVF

   private val layoutXml = IO.readFileString("Konfiguracja/search/" + repository.model.name + ".xml")
   private val searchDefinition = SearchDefinition.parseXML(layoutXml)

   private val searchResults = new SearchResultTable[CommonFeaturesRow](
      new CommonFeaturesColumnFactory(searchDefinition.columns).makeColumns,
      parentEV
   )

   private val rootPane = new ScrollPane()
   rootPane.padding = Insets(10)

   private val textInfo = new Label()
   private def setTextInfo(str: String): Unit = Platform.runLater {
      textInfo.text = str
   }

   rootPane.content = new VBox(10,
      textInfo,
      searchResults
   )

   root = rootPane

   private def makeSearchTask(): Task[Unit] = new Task[Unit]() {
      override def call(): Unit = {
         setTextInfo("Wyszukiwanie punktów wspólnych w toku...")
         val base = repository.fetchRecord(baseRid).getOrElse(throw new RuntimeException("Record for which common features are being searched ceased to exist"))
         val fs = searchDefinition.commonFeatureSet
         val interestingRecords = repository.fetchAllRecords()
            .map({
               case (rid, dmv) =>
                  new CommonFeaturesRow(
                     CommonFeatures.countCommonFeatures(fs, base, dmv),
                     rid,
                     () => repository.ridSet.getTemporaryIndex(rid).toInt,
                     dmv
                  )
            })
            .filter(r => r.commonFeatures.amount >= 4 && r.rid != baseRid)
            .sortBy(_.commonFeatures)(FeaturesEqualImportanceOrder).reverse

         println(s"Found ${interestingRecords.length} records")
         setTextInfo(s"Znaleziono ${interestingRecords.length} rekordów")
         Platform.runLater {
            searchResults.items.get().setAll(interestingRecords:_*)
         }
      }
   }

   private def setComputeInProgress(isComputing: Boolean): Unit = Platform.runLater {
      this.setCursor(if (isComputing) Cursor.Wait else Cursor.Default)
   }

   private def runSearch(): Unit = {
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

   runSearch()
}
