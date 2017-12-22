package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.ModelDefinition

import scalafx.scene.control.Label
import scalafx.scene.layout.Pane

class EditableModelView(val modelDefinition: ModelDefinition) extends Pane {
   children = Seq(
      Label("Model for " + modelDefinition.name)
   )
}
