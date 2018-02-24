package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class EditRecords extends Scene {

   // TODO these are for testing, later this will be encapsulated into separate functions etc.
   val modelText: String =
      """
        |{
        |  "name": "Testowy",
        |  "types": {
        |    "płeć": ["M","K"],
        |    "rodzic": {
        |      "Imię": "string",
        |      "Nazwisko": "string",
        |      "Zawód": "string"
        |    }
        |  },
        |  "fields": {
        |    "Miasto": "string",
        |    "Imię": "string",
        |    "Nazwisko": "string",
        |    "Data urodzenia": "date",
        |    "Płeć": "płeć",
        |    "Ojciec": "rodzic",
        |    "Matka": "rodzic",
        |    "Źródło": "bigtext"
        |  }
        |}
      """.stripMargin
   val model: Model = Model.fromDefinition(modelText).get
   // TODO loading the file
   val editableView: EditableView = EditableView.makeFromDefinition(model,
      """
        |<vbox>
        |   <label>Osoba</label>
        |    <hbox>
        |        <vbox>
        |            <TextField path="Imię"/>
        |            <TextField path="Nazwisko"/>
        |            <TextField path="Miasto"/>
        |            <choicefield path="Płeć"/>
        |        </vbox>
        |        <vbox>
        |            <Label>Ojciec</Label>
        |            <TextField path="Ojciec.Imię"/>
        |            <TextField path="Ojciec.Nazwisko"/>
        |            <TextField path="Ojciec.Zawód"/>
        |        </vbox>
        |
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