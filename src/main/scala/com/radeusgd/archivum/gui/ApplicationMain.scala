package com.radeusgd.archivum.gui

import com.radeusgd.archivum.gui.scenes.EditRecords

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
      scene = EditRecords.instance //MainMenu.instance // for debugging it's faster to skip menu
   }
}
