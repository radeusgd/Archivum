package com.radeusgd.archivum.gui

import java.io.{PrintWriter, StringWriter}

import com.radeusgd.archivum.gui.scenes.{EditRecords, MainMenu}

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, TextArea}
import scalafx.scene.control.Alert.AlertType

object ApplicationMain extends JFXApp {

   def switchScene(scene: Scene): Unit = {
      stage.scene = scene
   }

   def reportException(throwable: Throwable): Unit = {
      val stackTrace = {
         val sw = new StringWriter
         throwable.printStackTrace(new PrintWriter(sw))
         sw.toString
      }

      new Alert(AlertType.Error) {
         initOwner(stage)
         title = "Fatal Error"
         headerText = "An exception has been thrown."
         contentText = throwable.getLocalizedMessage
         dialogPane().expandableContentProperty().setValue(TextArea.sfxTextArea2jfx(new TextArea {
            text = stackTrace
            editable = false
            maxWidth = Double.MaxValue
            maxHeight = Double.MaxValue
         }))
      }.showAndWait()

   }

   private val defaultWidth = 600
   private val defaultHeight = 450

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = defaultWidth
      height = defaultHeight
      scene = MainMenu.instance
   }

   Thread.setDefaultUncaughtExceptionHandler((th, ex) => reportException(ex))
}
