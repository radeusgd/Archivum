package com.radeusgd.archivum.datamodel

import java.time.LocalDate

sealed class DMValue {
   def asInt: DMInteger = throw new ClassCastException
   def asString: DMString = throw new ClassCastException
   def asDate: DMDate = throw new ClassCastException
   def asArray: DMArray = throw new ClassCastException
   def asStruct: DMStruct = throw new ClassCastException
}

case class DMInteger(val value: Int) extends DMValue {
   override def asInt: DMInteger = this
}
case class DMString(val value: String) extends DMValue {
   override def asString: DMString = this
}

// TODO  FIXME  historic dates support!
case class DMDate(val value: LocalDate) extends DMValue {
   override def asDate: DMDate = this
}

case class DMArray(val values: Vector[DMValue]) extends DMValue {
   override def asArray: DMArray = this
   def updated(idx: Int, v: DMValue): DMArray = DMArray(values.updated(idx, v))
}

case class DMStruct(val values: Map[String, DMValue]) extends DMValue {
   override def asStruct: DMStruct = this
   def updated(key: String, v: DMValue) = DMStruct(values.updated(key, v))
}