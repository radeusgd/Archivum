package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.gui.{ApplicationMain, EditableView}
import com.radeusgd.archivum.persistence.Database

import scalafx.Includes._
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
        |    "Numer domu": "integer",
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

   private val localLogLabel = new Label("")

   private def logLocal(msg: String): Unit = {
      localLogLabel.text = localLogLabel.text.value + msg + "\n"
   }

   val db = Database.open()

   val box = new VBox()

   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(MainMenu.instance)
            }
         },
         new Button("Create repository") {
            onAction = handle {
               db.createRepository(modelText)
            }
         },
         new Button("Open repository") {
            onAction = handle {
               val repo = db.openRepository(model.name).get
               val editableView: EditableView = EditableView.makeFromDefinition(repo,
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
               box.children = editableView
               val rids = repo.fetchAllIds()
               // TODO will need to handle going over all items in DB, arrows, i / n label etc.
               val current =
                  if (rids.isEmpty) repo.createRecord(model.roottype.makeEmpty)
                  else rids.head
               editableView.setModelInstance(current)
            }
         },
         new Button("Test") {
            onAction = handle {
               val repo = db.openRepository("Testowy").get

               def test(): Unit = {
                  val r = repo.createRecord(model.roottype.makeEmpty)
                  logLocal(s"Created $r")
               }

               test()
               test()
               val rid = repo.createRecord(model.roottype.makeEmpty)
               repo.updateRecord(rid, model.roottype.makeEmpty)
               test()
               test()

               val res = repo.fetchAllRecords()
               logLocal(res.toString())
            }
         },
         localLogLabel,
         box
      )
   }
}

object EditRecords {
   lazy val instance = new EditRecords()
}