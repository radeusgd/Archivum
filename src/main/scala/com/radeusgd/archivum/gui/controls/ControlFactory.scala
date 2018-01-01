package com.radeusgd.archivum.gui.controls

import scalafx.scene.layout.Pane

import com.radeusgd.archivum.datamodel.types._

object ControlFactory {
   def createControl(field: Field): Pane = {
      val name = field.getName
      field match {
         case longString: LongStringField => new LongText(name, longString)
         case simpleString: StringField => new SimpleText(name, simpleString)
         case enum: EnumField => new EnumChoice(name, enum)
         case unknown => new SimpleText(name, unknown)
      }
   }
}
