package com.radeusgd.archivum.gui.scenes

import java.io.{File, PrintWriter}

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository

import scalafx.Includes.handle
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

class ExportRepository(val repository: Repository, parentScene: Scene) extends Scene {

   private val formats: List[String] = List("JSON", "CSV")
   private val formatChoice = new ComboBox[String](formats) {
      value = formats.head
   }

   private def chooseSaveFile(ext: ExtensionFilter): Option[File] = {
      val fileChooser = new FileChooser {
         title = "Export to " + ext.description
         initialFileName = repository.model.name + ext.extensions.head.substring(1)
         extensionFilters.addAll(ext)
      }

      Option(fileChooser.showSaveDialog(delegate.getWindow))
   }

   private def exportJson(file: File): Unit = {
      val jsons = repository.fetchAllRecords()
         .map({ case (_, v) => repository.model.roottype.toHumanJson(v) })
      val pw = new PrintWriter(file)
      jsons.foreach(j => pw.println(j.compactPrint))
      pw.close()
      utils.showInfo("Repository exported to " + file.getPath)
   }

   content = new VBox(
      utils.makeGoToButton("< Powrót", parentScene),
      new HBox(
         new Label("Filter: "),
         new TextField {
            disable = true
            text = "funkcja w budowie"
         }
      ),
      formatChoice,
      new Button("Eksport") {
         onAction = handle {
            formatChoice.value.value match {
               case "JSON" =>
                  chooseSaveFile(new ExtensionFilter("JSON list", "*.dbjson")) match {
                     case None => utils.showError("No file selected")
                     case Some(file) => exportJson(file)
                  }
               case "CSV" => utils.notImplemented()
               case _ => throw new RuntimeException("Unsupported export format")
            }
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

