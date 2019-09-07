package com.radeusgd.archivum.gui

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Files

import com.radeusgd.archivum.gui.ApplicationMain.stage
import javafx.concurrent.Task
import javafx.scene.control.Dialog
import org.slf4j.LoggerFactory
import scalafx.Includes.handle
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.layout.VBox
import scalafx.stage.{DirectoryChooser, Modality}

package object utils {

   def makeGoToButton(name: String, targetScene: => Scene): Button =
      new Button(name) {
         onAction = handle {
            ApplicationMain.switchScene(targetScene)
         }
      }

   def makeGoToButtonRefreshable(name: String, targetScene: => Scene with Refreshable): Button =
      new Button(name) {
         onAction = handle {
            ApplicationMain.switchScene(targetScene)
            Platform.runLater {
               targetScene.refresh()
            }
         }
      }

   def mkButton(name: String, action: () => Unit): Button =
      new Button(name) {
         onAction = handle {
            action()
         }
      }

   def showMessage(header: String, content: String = "", alertType: AlertType = AlertType.Warning): Unit = {
      new Alert(alertType) {
         title = "Error"
         headerText = header
         contentText = content
      }.showAndWait()
   }

   def showWarning(header: String, content: String = ""): Unit = showMessage(header, content, AlertType.Warning)

   def showError(header: String, content: String = ""): Unit = showMessage(header, content, AlertType.Error)

   def showInfo(header: String, content: String = ""): Unit = showMessage(header, content, AlertType.Information)

   def reportException(message: String, throwable: Throwable): Unit = {
      val stackTrace = {
         val sw = new StringWriter
         throwable.printStackTrace(new PrintWriter(sw))
         sw.toString
      }

      LoggerFactory.getLogger("Unhandled Exception").error(stackTrace)
      throwable.printStackTrace()

      val systemInfo = System.getProperties.toString

      val alertText =
         stackTrace + "\n\n" + systemInfo

      new Alert(AlertType.Error) {
         initOwner(stage)
         title = "Fatal Error"
         headerText = message
         contentText = throwable.getLocalizedMessage
         dialogPane().expandableContentProperty().setValue(TextArea.sfxTextArea2jfx(new TextArea {
            text = alertText
            editable = false
            maxWidth = Double.MaxValue
            maxHeight = Double.MaxValue
         }))
      }.showAndWait()

   }

   def notImplemented(): Unit =
      showError("Feature unavailable", "This feature hasn't been implemented yet.")

   def ask(question: String, comment: String = ""): Boolean = {
      val alert = new Alert(AlertType.Confirmation) {
         headerText = question
         contentText = comment
      }
      val result = alert.showAndWait()
      result match {
         case Some(ButtonType.OK) => true
         case _ => false
      }
   }

   def chooseSaveDirectory(dialogTitle: String, initialPath: File): Option[File] = {
      Files.createDirectories(initialPath.toPath)
      val fileChooser = new DirectoryChooser {
         title = dialogTitle
         initialDirectory = initialPath
      }

      Option(fileChooser.showDialog(ApplicationMain.stage.scene.delegate.getValue.getWindow))
   }

   def runTaskWithProgress[A](dialogTitle: String, task: Task[A]): Unit = {

      val dialog = new Dialog[Void]
      dialog.initModality(Modality.None)
      dialog.setWidth(500)
      dialog.setHeight(80)

      val pane = new DialogPane()
      dialog.setDialogPane(pane)
      val progressBar = new ProgressBar()
      progressBar.setPrefWidth(470)
      val statusText = new Label()

      pane.content = new VBox(7, statusText, progressBar)
      dialog.setTitle(dialogTitle)

      progressBar.progress.unbind()
      progressBar.progress.bind(task.progressProperty())
      statusText.text.bind(task.messageProperty())

      def allowClose(): Unit = {
         dialog.getDialogPane.getButtonTypes.add(new ButtonType("OK", ButtonData.CancelClose))
      }

      task.setOnFailed(_ => {
         utils.reportException("Task failed", task.getException)
         statusText.text.unbind()
         statusText.text = "Failed: " + task.getException.toString

         allowClose()
      })

      task.setOnSucceeded(_ => {
         statusText.text.unbind()
         statusText.text = "Zakończono"
         allowClose()
      })

      ApplicationMain.registerLongRunningTask(task)
      Platform.runLater(dialog.show())
      val t = new Thread(task)
      t.start()
   }
}
