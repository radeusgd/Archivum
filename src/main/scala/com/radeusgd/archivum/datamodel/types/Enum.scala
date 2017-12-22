package com.radeusgd.archivum.datamodel.types

class EnumField(val enumType: EnumType, private var v: String) extends Field {

   override def set(value: String): Boolean = {
      if (enumType.isValid(value)) {
         v = value
         true
      } else false
   }

   override def get: String = v
}

class EnumType(val values: IndexedSeq[String]) extends FieldType {
   override def createEmptyField(): Field = new EnumField(this, values(0))

   def isValid(value: String): Boolean = {
      for (p <- values) {
         if (value == p) return true
      }
      false
   }
}