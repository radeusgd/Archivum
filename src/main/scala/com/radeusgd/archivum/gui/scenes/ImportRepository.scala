package com.radeusgd.archivum.gui.scenes

import java.io.File

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO
import spray.json._

import scala.util.control.NonFatal
import scalafx.Includes.handle
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

class ImportRepository(val repository: Repository, parentScene: Scene) extends Scene {

   private def chooseOpenFile(): Option[File] = {
      val fileChooser = new FileChooser {
         title = "Import from JSON"
         extensionFilters.addAll(
            new ExtensionFilter("JSON list", "*.dbjson"),
            new ExtensionFilter("All Files", "*.*")
         )
      }

      Option(fileChooser.showOpenDialog(delegate.getWindow))
   }

   private def importJson(file: File): Unit = {
      val source = io.Source.fromFile(file)
      try {
         val lines = source.getLines()
         val records = lines.map(line => repository.model.roottype.fromHumanJson(line.parseJson))
         // FIXME TODO this loads all records until there is an error,
         // we might prefer to discard all of them if any one is wrong
         io.Source.fromFile(file)
         records.foreach(er => er.fold(throw _, repository.createRecord))
         utils.showInfo("All records imported")
      } catch {
         case NonFatal(e) => utils.reportException("There was an error during import, only some records have been imported.", e)
      } finally {
         source.close()
      }
   }

   content = new VBox(
      utils.makeGoToButton("< Powrót", parentScene),
      new Label("Zaimporotwane rekordy zostaną dopisane do bazy (rekordy już znajdujące się w bazie pozostaną niezmienione)"),
      new Label("Wspierane jest tylko importowanie z formatu JSON"),
      new Button("Import") {
         onAction = handle {
            chooseOpenFile() match {
               case None => utils.showError("No file selected")
               case Some(file) => importJson(file)
            }
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

