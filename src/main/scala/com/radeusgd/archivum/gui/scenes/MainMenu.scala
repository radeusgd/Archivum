package com.radeusgd.archivum.gui.scenes

import java.io.File

import com.radeusgd.archivum.gui.{ApplicationMain, utils}
import com.radeusgd.archivum.persistence.Database
import com.radeusgd.archivum.utils.IO

import scala.util.control.NonFatal
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
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
         val modelDefinition = IO.readFileString(fname)
         db.createRepository(modelDefinition)
         refreshRepos()
      } catch {
         case NonFatal(e) => utils.reportException("Error importing model", e)
      }
   }

   content = new VBox {
      spacing = 10
      padding = Insets(15.0)
      children = Seq(
         new Button("Create repository") {
            onAction = handle {
               val d = new File("models")
               if (!d.exists || !d.isDirectory) {
                  throw new RuntimeException("Models directory doesn't exist")
               }
               val availableModels = d.listFiles().filter(_.isFile).map(_.getPath).toList
               if (availableModels.isEmpty) {
                  throw new RuntimeException("Models directory is empty")
               }
               val dialog = new ChoiceDialog[String](availableModels.head, availableModels) {
                  title = "Model name"
                  headerText = "Importing model."
                  contentText = "Model:"
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