package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types.FieldType

class Model(
                        val name: String,
                        val fields: Map[String, FieldType]/*,
                        val fieldsOrder: Seq[String]*/    //TODO layout!
                     ) {

}


/*
implicit object MoneyFormat extends JsonFormat[Money] {
  val fmt = """([A-Z]{3}) ([0-9.]+)""".r
  def write(m: Money) = JsString(s"${m.currency} ${m.amount}")
  def read(json: JsValue) = json match {
    case JsString(fmt(c, a)) => Money(c, BigDecimal(a))
    case _ => deserializationError("String expected")
  }
}
 */