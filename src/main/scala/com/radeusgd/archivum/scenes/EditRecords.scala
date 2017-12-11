package com.radeusgd.archivum.scenes

import com.radeusgd.archivum.ApplicationMain

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class EditRecords extends Scene {
   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(MainMenu.instance)
            }
         },
         Label("TODO"),
         Label("dwa"),
      )
   }
}

object EditRecords {
   lazy val instance = new EditRecords()
}