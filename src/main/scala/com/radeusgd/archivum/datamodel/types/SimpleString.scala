package com.radeusgd.archivum.datamodel.types

class StringField(private val name: String, private var v: String) extends Field {

   override def set(value: String): Boolean = {
      v = value
      true // simple string accepts everything, TODO maybe some length limit + long text type
   }

   override def get: String = v

   override def getName: String = name
}

object SimpleString extends FieldType {
   override def createEmptyField(name: String): Field = new StringField(name, "")
}

class LongStringField(name: String, v: String) extends StringField(name, v)

object LongString extends FieldType {
   override def createEmptyField(name: String): Field = new LongStringField(name, "")
}