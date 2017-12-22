package com.radeusgd.archivum.datamodel.types

class StringField(private var v: String) extends Field {

   override def set(value: String): Boolean = {
      v = value
      true // simple string accepts everything, TODO maybe some length limit + long text type
   }

   override def get: String = v
}

object SimpleString extends FieldType {
   override def createEmptyField(): Field = new StringField("")
}