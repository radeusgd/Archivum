package com.radeusgd.archivum.gui

import scalafx.Includes.handle
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ButtonType}

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

   def showError(header: String, content: String = "", alertType: AlertType = AlertType.Warning): Unit = {
      new Alert(alertType) {
         title = "Error"
         headerText = header
         contentText = content
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
         case _                   => false
      }
   }
}
