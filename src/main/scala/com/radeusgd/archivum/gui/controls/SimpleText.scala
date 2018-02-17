package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.ViewFactory
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.geometry.Pos
import scalafx.scene
import scalafx.scene.control.{Label, TextField, TextInputControl}
import scalafx.scene.layout.HBox

class SimpleText(val label: String, path: Seq[String]) extends HBox {
   protected val textField: TextInputControl = new TextField()
   spacing = 5
   children = Seq(
      new Label(label) {
         minWidth = 100
         alignment = Pos.CenterRight
      },
      textField
   )
}

object SimpleTextFactory extends SimpleFactory(new SimpleText(_, _)) {
   override val nodeType: String = ViewLanguage.TextField
}
