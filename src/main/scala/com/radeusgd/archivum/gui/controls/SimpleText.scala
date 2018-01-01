package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.types.{Field, FieldType}

import scalafx.geometry.Pos
import scalafx.scene.control.{Label, TextField, TextInputControl}
import scalafx.scene.layout.HBox

//TODO revise this!!!
class SimpleText(val name: String, val field: Field) extends HBox {
   protected val textField: TextInputControl = new TextField()
   spacing = 5
   children = Seq(
      new Label(name) {
         minWidth = 100
         alignment = Pos.CenterRight
      },
      textField
   )
}
