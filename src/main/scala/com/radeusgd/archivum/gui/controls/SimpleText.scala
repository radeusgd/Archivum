package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.types.{Field, FieldType}

import scalafx.scene.control.{Label, TextField}
import scalafx.scene.layout.HBox

//TODO revise this!!!
class SimpleText(val name: String, val field: Field) extends HBox {
   private val textField = new TextField()
   spacing = 5
   children = Seq(
      Label(name),
      textField
   )
}
