package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.datamodel.ModelJsonProtocol._
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}
import spray.json.JsonParser

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class EditRecords extends Scene {

   // TODO these are for testing, later this will be encapsulated into separate functions etc.
   val modelJson = JsonParser(
      """
        |{
        |  "name": "Testowy",
        |  "types": {
        |    "płeć": ["M","K"],
        |    "rodzic": {
        |      "imię": "string",
        |      "nazwisko": "string",
        |      "zawód": "strind"
        |    }
        |  },
        |  "fields": {
        |    "Miasto": "string",
        |    "Imię": "string",
        |    "Nazwisko": "string",
        |    "Data urodzenia": "date",
        |    "Płeć": "płeć",
        |    "Źródło": "bigtext"
        |  }
        |}
      """.stripMargin)
   val model: Model = modelJson.convertTo[Model]
   // TODO loading the file
   val editableView: EditableView = EditableView.makeFromDefinition(model,
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