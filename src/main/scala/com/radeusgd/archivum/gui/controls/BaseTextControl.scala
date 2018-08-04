package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.StringDMBridge

import scalafx.geometry.Pos
import scalafx.scene.control.{Label, TextField, TextInputControl, Tooltip}
import scalafx.scene.layout.HBox

class BaseTextControl(bridge: StringDMBridge,
                      val properties: CommonProperties,
                      path: List[String],
                      protected val editableView: EditableView)
   extends HBox with BoundControl {
   protected val textField: TextInputControl = new TextField() {
      prefWidth = 200 // TODO setting width
   }
   spacing = LayoutDefaults.defaultSpacing
   alignment = Pos.BaselineRight
   children = if (properties.label == "") Seq(textField) else Seq(
      new Label(properties.label) {
         minWidth = LayoutDefaults.defaultFieldNameWidth
         alignment = Pos.CenterRight
      },
      textField
   )

   protected val fieldGetter: DMValue => DMValue = DMUtils.makeGetter(path)
   protected val fieldSetter: (DMAggregate, DMValue) => DMAggregate = DMUtils.makeSetter(path)

   protected def fromValue(v: DMValue): String = bridge.fromDM(v)

   protected def toValue(s: String): DMValue = bridge.fromString(s)

   textField.text.onChange((_, _, newValue) => {
      editableView.update(fieldSetter(_, toValue(newValue)))
   })

   override def refreshBinding(newValue: DMStruct): Unit = {
      /*
       TODO warning - this can be destructive
       (if the element had something else than String or Null,
       but typechecking should make sure this won't happen
        */
      textField.text = fromValue(fieldGetter(newValue))
   }

   private val errorTooltip = Tooltip("")

   // TODO this could be some mix-in or sth
   override def refreshErrors(errors: List[ValidationError]): List[ValidationError] = {
      val (myErrors, otherErrors) = errors.partition(_.getPath == path)
      if (myErrors.isEmpty) {
         textField.setStyle("")
         Tooltip.uninstall(textField, errorTooltip)
         errorTooltip.hide()
      } else {
         textField.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;")
         Tooltip.install(textField, errorTooltip)
         val texts = myErrors.map(_.getMessage)
         errorTooltip.text = texts.mkString("\n")
         val pos = textField.localToScreen(textField.prefWidth.value, 0)
         errorTooltip.show(textField, pos.getX, pos.getY)
      }

      otherErrors
   }

   override def augmentFreshValue(newValue: DMStruct): DMStruct = {
      val v = super.augmentFreshValue(newValue) // apply any parent modifications (probably none)
      if (properties.sticky) {
         ???
      } else if (properties.default.isDefined) {
         fieldSetter(v, toValue(properties.default.get)).asInstanceOf[DMStruct]
      } else {
         v
      }
   }
}