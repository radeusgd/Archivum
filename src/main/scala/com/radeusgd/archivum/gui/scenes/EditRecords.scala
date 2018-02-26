package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}
import com.radeusgd.archivum.persistence.Database

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
        |    "Data urodzenia": "string",
        |    "Płeć": "płeć",
        |    "Ojciec": "rodzic",
        |    "Matka": "rodzic",
        |    "Źródło": "bigtext"
        |  }
        |}
      """.stripMargin // TODO date is not yet implemented
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

   private val localLogLabel = new Label("")

   private def logLocal(msg: String): Unit = {
      localLogLabel.text = localLogLabel.text.value + msg + "\n"
   }

   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(MainMenu.instance)
            }
         },
         new Button("Test") {
            onAction = handle {
               val db = Database.open()
               val mdl = Model.fromDefinition(modelText).get
               db.createRepository(modelText)
               val repo = db.openRepository("Testowy").get
               def test(): Unit = {
                  val r = repo.createRecord(mdl.roottype.makeEmpty)
                  logLocal(s"Created $r")
               }

               test()
               test()
               val rid = repo.createRecord(mdl.roottype.makeEmpty)
               repo.updateRecord(rid, mdl.roottype.makeEmpty)
               test()
               test()

               val res = repo.fetchAllRecords()
               logLocal(res.toString())
            }
         },
         localLogLabel,
         editableView
      )
   }
}

object EditRecords {
   lazy val instance = new EditRecords()
}