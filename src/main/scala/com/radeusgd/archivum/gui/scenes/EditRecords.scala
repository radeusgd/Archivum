package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class EditRecords extends Scene {

   // TODO loading the file
   val editableView: EditableView = EditableView.makeFromDefinition(
      """
        |<vbox>
        |   <label>Testing</label>
        |    <hbox>
        |        <vbox>
        |            <TextField label="A" path="a"/>
        |            <TextField label="B" path="a"/>
        |        </vbox>
        |        <vbox>
        |            <TextField  label="C" path="a"/>
        |            <TextField  label="D" path="a"/>
        |        </vbox>
        |    </hbox>
        |</vbox>
      """.stripMargin)

   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(MainMenu.instance)
            }
         },
         Label("TODO"),
         editableView
      )
   }
}

object EditRecords {
   lazy val instance = new EditRecords()
}