package com.radeusgd.archivum.gui

import com.radeusgd.archivum.gui.scenes.MainMenu

import scalafx.application.JFXApp
import scalafx.scene.Scene

object ApplicationMain extends JFXApp {

   def switchScene(scene: Scene): Unit = {
      stage.scene = scene
   }

   private val defaultWidth = 600
   private val defaultHeight = 450

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = defaultWidth
      height = defaultHeight
      scene = MainMenu.instance
   }

   Thread.setDefaultUncaughtExceptionHandler((th, ex) => utils.reportException("There was an unhandled error", ex))
}
