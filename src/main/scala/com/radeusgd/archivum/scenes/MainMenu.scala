package com.radeusgd.archivum.scenes

import com.radeusgd.archivum.ApplicationMain

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class MainMenu extends Scene {
   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         Label("test"),
         Label("dwa"),
         new Button("Browse") {
            onAction = handle {
               ApplicationMain.switchScene(EditRecords.instance)
            }
         }
      )
   }
}

object MainMenu {
   lazy val instance = new MainMenu()
}