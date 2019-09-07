package com.radeusgd.archivum.gui.scenes

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import scalafx.Includes.handle
import javafx.concurrent.Task
import org.controlsfx.dialog.ProgressDialog
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

class ExportRepository(val repository: Repository, parentScene: Scene) extends Scene {

   private val ZIP: String = "ZIP (z mediami)"
   private val formats: List[String] = List("JSON", /*"CSV",*/ ZIP)
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
      val task = new Task[Unit] {
         override def call(): Unit = {
            updateMessage("Pobieranie rekordów")
            val all = repository.fetchAllRecords()

            var progress = 0
            val todo = 2 * all.length
            def bumpProgress(): Unit = {
               progress += 1
               updateProgress(progress, todo)
            }

            updateMessage("Serializacja rekordow")

            val jsons = all
               .map({ case (_, v) => bumpProgress(); repository.model.roottype.toHumanJson(v) })

            updateMessage("Zapis rekordów")

            val pw = new PrintWriter(file)
            jsons.foreach(j => { bumpProgress(); pw.println(j.compactPrint) })
            pw.close()
         }
      }
      utils.runTaskWithProgress("Eksport JSON", task)
   }

   private class ZipHelper(outputFile: File) {
      private val stream = new ZipOutputStream(new FileOutputStream(outputFile))
      stream.setMethod(ZipOutputStream.DEFLATED)
      //stream.setLevel()

      def close(): Unit = stream.close()

      def addFromWriter(path: String, write: PrintWriter => Unit): Unit = {
         val e = new ZipEntry(path)
         stream.putNextEntry(e)
         val writer = new PrintWriter(new OutputStream {
            override def write(b: Int): Unit = stream.write(b)
         })
         write(writer)
         writer.close()
         stream.closeEntry()
      }

      def addFile(path: String, file: File): Unit = {
         if (!file.isFile) throw new RuntimeException(file.toString + " is not a valid file")
         val entry = new ZipEntry(path)
         //entry.setLastModifiedTime() // TODO
         stream.putNextEntry(entry)
         //TODO for big files this will be an issue
         val reader = new FileInputStream(file)
         stream.write(reader.readAllBytes())
         reader.close()
         stream.closeEntry()
      }

      def addDirectory(path: String, dir: File): Unit = {
         if (!dir.isDirectory) throw new RuntimeException(dir.toString + " is not a valid directory")
         val files = dir.listFiles()
         files.foreach(f =>
            if (f.isFile) addFile(path + "/" + f.getName, f)
            else if (f.isDirectory) addDirectory(path + "/" + f.getName, f)
         )
      }
   }

   private def exportZip(file: File): Unit = {
      val task = new Task[Unit] {
         override def call(): Unit = {
            updateMessage("Pobieranie rekordów")
            val all = repository.fetchAllRecords()
            val files = new File("Fotografie/").listFiles()

            var progress = 0
            val todo = 2 * all.length + files.length
            def bumpProgress(): Unit = {
               progress += 1
               updateProgress(progress, todo)
            }

            updateMessage("Serializacja rekordow")
            val jsons = all
               .map({ case (_, v) => bumpProgress(); repository.model.roottype.toHumanJson(v) })

            val zip = new ZipHelper(file)

            updateMessage("Zapis rekordów")
            zip.addFromWriter(
               "rekordy.dbjson",
               pw => jsons.foreach(j => { bumpProgress(); pw.println(j.compactPrint) })
            )

            updateMessage("Zapis fotografii")
            //zip.addDirectory("Fotografie", new File("Fotografie/"))
            files.foreach(f => {
               bumpProgress()
               if (f.isFile) zip.addFile("Fotografie/" + f.getName, f)
               else utils.showError("Niespodziewany plik w katalogu Fotografie",
                  "Plik " + f.toString + " zostanie pominięty przy eksporcie")
            })
            zip.close()
         }
      }
      utils.runTaskWithProgress("Eksport ZIP", task)
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
               case ZIP =>
                  chooseSaveFile(new ExtensionFilter("ZIP", "*.zip")) match {
                     case None => utils.showError("No file selected")
                     case Some(file) => exportZip(file)
                  }
               case _ => throw new RuntimeException("Unsupported export format")
            }
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

