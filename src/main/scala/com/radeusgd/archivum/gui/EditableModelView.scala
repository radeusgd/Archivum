package com.radeusgd.archivum.gui

import com.radeusgd.archivum.datamodel.{ModelDefinition, ModelInstance}
import com.radeusgd.archivum.gui.controls.SimpleText

import scala.collection.immutable
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, VBox}

class EditableModelView(private val instance: ModelInstance) extends Pane {

   children = new VBox {
      children = Seq(Label("Editable Model View")) ++ makeControls()
   }

   private def makeControls(): Seq[Pane] = {
      (instance.fields map (new SimpleText(_, _)).tupled).toSeq
   }
}
