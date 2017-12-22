package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.datamodel.Repository
import com.radeusgd.archivum.gui.{ApplicationMain, EditableModelView}

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox

class MockEditor extends Scene {
   val repo = Repository.open("testdb/model.json") // TODO repositories should be managed centrally
   content = new VBox {
      padding = Insets(5.0)
      children = Seq(
         new Button("< Back") {
            onAction = handle {
               ApplicationMain.switchScene(MainMenu.instance)
            }
         },
         new EditableModelView(repo.newInstance())
      )
   }
}

object MockEditor {
   lazy val instance = new MockEditor()
}