package com.radeusgd.archivum.gui

import scalafx.Includes.handle
import scalafx.application.Platform
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button}

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

   def showError(header: String, content: String = "", alertType: AlertType = AlertType.Warning): Unit = {
      new Alert(alertType) {
         title = "Error"
         headerText = header
         contentText = content
      }.showAndWait()
   }

   def notImplemented(): Unit =
      showError("Feature unavailable", "This feature hasn't been implemented yet.")

}
