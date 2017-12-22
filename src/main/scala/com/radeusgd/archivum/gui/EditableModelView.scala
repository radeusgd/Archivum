package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.{ModelDefinition, ModelInstance}

import scalafx.scene.control.Label
import scalafx.scene.layout.{VBox, Pane}

class EditableModelView(private val instance: ModelInstance) extends Pane {

   children = new VBox {
      children = Seq(Label("Editable Model View")) ++ makeControls()
   }

   private def makeControls(): Seq[Pane] = {
      Nil
   }
}
