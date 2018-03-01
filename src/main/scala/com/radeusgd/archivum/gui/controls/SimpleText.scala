package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.geometry.Pos
import scalafx.scene.control.{Label, TextField, TextInputControl}
import scalafx.scene.layout.HBox

class SimpleText(val label: String, path: List[String], protected val editableView: EditableView) extends HBox with BoundControl {
   protected val textField: TextInputControl = new TextField()
   spacing = 5
   children = Seq(
      new Label(label) {
         minWidth = 100
         alignment = Pos.CenterRight
      },
      textField
   )

   protected val fieldGetter: DMAggregate => DMValue = DMUtils.makeGetter(path)
   protected val fieldSetter: (DMStruct, DMValue) => DMStruct = DMUtils.makeSetter(path)

   textField.text.onChange((_, _, newValue) => {
      editableView.update(fieldSetter(_, DMString(newValue)))
   })

   override def refreshBinding(newValue: DMStruct): Unit = {
      /*
       TODO warning - this can be destructive
       (if the element had something else than String or Null,
       but typechecking should make sure this won't happen
        */
      textField.text = fieldGetter(newValue).asString.getOrElse("")
   }
}

object SimpleTextFactory extends SimpleControlFactory(new SimpleText(_, _, _)) {
   override val nodeType: String = ViewLanguage.TextField
}
