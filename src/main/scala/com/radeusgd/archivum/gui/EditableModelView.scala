package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.{ModelDefinition, ModelInstance}
import com.radeusgd.archivum.gui.controls.{ControlFactory, SimpleText}

import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}

class EditableModelView(private val instance: ModelInstance) extends Pane {

   children = new VBox {
      spacing = 7
      children = makeControls()
   }

   private def makeControls(): Seq[Pane] = {
      (instance.fields.values map ControlFactory.createControl).toSeq
   }
}
