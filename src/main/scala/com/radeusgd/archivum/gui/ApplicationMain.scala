package com.radeusgd.archivum.gui

import com.radeusgd.archivum.gui.scenes.EditRecords

import scalafx.application.JFXApp
import scalafx.scene.Scene

object ApplicationMain extends JFXApp {

   def switchScene(scene: Scene): Unit = {
      stage.scene = scene
   }

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = 600
      height = 450
      scene = EditRecords.instance//MainMenu.instance // for debugging it's faster to skip menu
   }
}
