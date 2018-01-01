package com.radeusgd.archivum.datamodel.types
import javafx.scene.layout.Pane

class EnumField(val enumType: EnumType, private val name: String, private var v: String) extends Field {

   override def set(value: String): Boolean = {
      if (enumType.isValid(value)) {
         v = value
         true
      } else false
   }

   override def get: String = v

   override def getName: String = name
}

class EnumType(val values: IndexedSeq[String]) extends FieldType {
   override def createEmptyField(name: String): Field = new EnumField(this, name, values(0))

   def isValid(value: String): Boolean = {
      for (p <- values) {
         if (value == p) return true
      }
      false
   }
}