package com.radeusgd.archivum

import com.radeusgd.archivum.scenes.MainMenu

import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.layout.VBox

object ApplicationMain extends JFXApp {

   def switchScene(scene: Scene): Unit = {
      stage.scene = scene
   }

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = 600
      height = 450
      scene = MainMenu.instance
   }
}
