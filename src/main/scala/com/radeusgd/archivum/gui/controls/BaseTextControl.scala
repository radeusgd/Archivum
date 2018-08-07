package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.commonproperties.{CommonProperties, DefaultValueOnCreation, HasCommonProperties, PreviosValueOnCreation}
import com.radeusgd.archivum.gui.controls.dmbridges.StringDMBridge
import scalafx.geometry.Pos
import scalafx.scene.control._
import scalafx.scene.layout.HBox

class BaseTextControl(bridge: StringDMBridge,
                      val properties: CommonProperties,
                      path: List[String],
                      protected val editableView: EditableView)
   extends HBox with BoundControl
   with DefaultValueOnCreation with PreviosValueOnCreation
{

   private val rows: Int = properties.rows.getOrElse(1)
   private val tfieldWidth: Double = properties.width.getOrElse(200).toDouble

   protected val textField: TextInputControl =
      if (rows == 1)
         new TextField() {
            prefWidth = tfieldWidth
         }
      else
         new TextArea() {
            prefWidth = tfieldWidth
            prefRowCount = rows
            wrapText = true
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
   protected val fieldSetter: (DMValue, DMValue) => DMValue = DMUtils.makeSetter(path)

   protected def fromValue(v: DMValue): String = bridge.fromDM(v)

   protected def toValue(s: String): DMValue = bridge.fromString(s)

   private var previousValue: DMValue = toValue("")

   textField.text.onChange((_, _, newValue) => {
      editableView.update(fieldSetter(_, toValue(newValue)))
      previousValue = toValue(newValue)
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

   override def commonProperties: CommonProperties = properties

   override def setValueFromOldOne(v: DMValue): DMValue = fieldSetter(v, previousValue)

   override def setDefaultFromString(v: DMValue, s: String): DMValue = fieldSetter(v, toValue(s))
}