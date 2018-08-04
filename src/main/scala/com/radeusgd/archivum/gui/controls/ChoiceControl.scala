package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.datamodel.types.EnumField
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage
import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.{ComboBox, Label}
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.HBox

// TODO add CommonProperties
class ChoiceControl(val label: String, path: List[String], protected val editableView: EditableView) extends HBox with BoundControl {

   val allowedValues: Seq[String] =
      editableView.model.roottype.getType(path).asInstanceOf[EnumField].values

   protected val choiceField: ComboBox[String] = new ComboBox[String](allowedValues) {
      value = allowedValues.head

      onKeyTyped = (evt: javafx.scene.input.KeyEvent) => {
         val char = evt.character
         // TODO locale?
         val chosenValue: Option[String] = allowedValues.find(choice => choice.toLowerCase.startsWith(char.toLowerCase))
         chosenValue.foreach(value => {
            choiceField.value = value
         })
      }
   }

   spacing = LayoutDefaults.defaultSpacing
   children = if (label == "") Seq(choiceField) else Seq(
      new Label(label) {
         minWidth = LayoutDefaults.defaultFieldNameWidth
         alignment = Pos.CenterRight
      },
      choiceField
   )

   protected val fieldGetter: DMValue => DMValue = DMUtils.makeGetter(path)
   protected val fieldSetter: (DMAggregate, DMValue) => DMAggregate = DMUtils.makeSetter(path)

   protected def setNewValue(value: String): Unit = {
      editableView.update(fieldSetter(_, DMString(value)))
   }

   choiceField.value.onChange((_, _, newValue) => setNewValue(newValue))

   override def refreshBinding(newValue: DMStruct): Unit = {
      /*
       TODO warning - this can be destructive
       (if the element had something else than String or Null,
       but typechecking should make sure this won't happen
        */
      choiceField.value = fieldGetter(newValue).asString.getOrElse("")
   }
}

object ChoiceControlFactory extends SimpleControlFactory(new ChoiceControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.ChoiceField
}
