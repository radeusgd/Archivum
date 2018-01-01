package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.types.Field

import scalafx.scene.control.{TextArea, TextInputControl}

class LongText(name: String, field: Field) extends SimpleText(name, field) {
   protected override val textField: TextInputControl = new TextArea() {
      prefRowCount = 5
   }
}
