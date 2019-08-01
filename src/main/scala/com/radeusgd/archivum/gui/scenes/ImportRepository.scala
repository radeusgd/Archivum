package com.radeusgd.archivum.gui.scenes

import java.io.File

import com.radeusgd.archivum.conversion.{JsonStructure, ModelStructure, Structure}
import com.radeusgd.archivum.datamodel.DMStruct
import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO
import spray.json._

import scala.util.control.NonFatal
import scalafx.Includes.handle
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextArea}
import scalafx.scene.layout.{HBox, VBox}
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
         val jsons = lines.map(_.parseJson).toSeq

         val newStructure = ModelStructure.extract(repository.model)
         val oldStructure = JsonStructure.unify(jsons)

         val diff = Structure.diff(oldStructure, newStructure)

         def pathToStr(p: List[String]): String = p.mkString(".")

         val removed =
            if (diff.missing.isEmpty) ""
            else "Pola, które zostaną usunięte:\n" ++ diff.missing.map(pathToStr).mkString("\n")

         val added =
            if (diff.added.isEmpty) ""
            else "Nowe pola, zostaną ustawione na puste wartości:\n" ++ diff.added.map(pathToStr).mkString("\n")

         val comment =
            if (removed.isEmpty && added.isEmpty) "Brak potrzeby konwersji bazy"
            else removed + "\n\n" + added + "\n\nJeśli mimo to chcesz kontynuować naciśnij OK."

         if (utils.ask("Czy napewno chcesz zaimportować " + jsons.length + " rekordów?", comment)) {
            val fixer =
               (js: JsValue) => JsonStructure.makeEmptyFields(repository.model, diff.added)(JsonStructure.dropFields(diff.missing)(js))

            val records = jsons.map(json => {
               val fixed = fixer(json)
               repository.model.roottype.fromHumanJson(fixed)
            })

            val valid: Seq[DMStruct] = records.map(er => er.fold(throw _, identity))
            valid.foreach(repository.createRecord)
            utils.showInfo("" + valid.length + " rekordów zostało pomyślnie dodanych do bazy")
         } else {
            utils.showInfo("Import anulowany")
         }
      } catch {
         case NonFatal(e) => utils.reportException("Podczas importu nastąpił błąd", e)
      } finally {
         source.close()
      }
   }

  val preprocessCode = new TextArea()

   content = new VBox(
      utils.makeGoToButton("< Powrót", parentScene),
      new Label("Zaimporotwane rekordy zostaną dopisane do bazy (rekordy już znajdujące się w bazie pozostaną niezmienione)"),
      new Label("Wspierane jest tylko importowanie z formatu JSON"),
      new HBox(new Label("Preprocess (zaawansowane):"), preprocessCode),
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

