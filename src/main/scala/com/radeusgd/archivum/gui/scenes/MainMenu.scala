package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.{ApplicationMain, utils}
import com.radeusgd.archivum.persistence.Database
import com.radeusgd.archivum.utils.IO

import scala.util.control.NonFatal
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ComboBox, TextInputDialog}
import scalafx.scene.layout.{HBox, VBox}

class MainMenu extends Scene {

   private val db = Database.open()
   private val repositoryChoice = new ComboBox[String]
   refreshRepos()

   def refreshRepos(): Unit = {
      val repos: Seq[String] = db.listRepositories()
      repositoryChoice.items.value.setAll(repos:_*)
      repos.headOption.foreach(firstOne => repositoryChoice.value = firstOne)
   }

   def importEmptyModel(fname: String): Unit = {
      try {
         val modelDefinition = IO.readFile(fname)
         db.createRepository(modelDefinition)
         refreshRepos()
      } catch {
         case NonFatal(e) => ApplicationMain.reportException(e)
      }
   }

   content = new VBox {
      spacing = 10
      padding = Insets(15.0)
      children = Seq(
         new Button("Create repository") {
            onAction = handle {
               val dialog = new TextInputDialog() {
                  title = "Model name"
                  headerText = "Importing model."
                  contentText = "Model filename (relative):"
               }

               val result = dialog.showAndWait()
               result.foreach(importEmptyModel)
            }
         },
         new HBox(
            repositoryChoice,
            new Button("Open") {
               onAction = handle {
                  db.openRepository(repositoryChoice.value.value) match {
                     case Some(repo) => ApplicationMain.switchScene(new RepositoryMenu(repo))
                     case None => utils.showError("Cannot open repository")
                  }
               }
            }
         )
      )
   }
}

object MainMenu {
   lazy val instance = new MainMenu()
}