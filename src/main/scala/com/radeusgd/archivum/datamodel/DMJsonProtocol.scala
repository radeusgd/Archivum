package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types._
import spray.json._

// TODO improve exceptions (better names, catch key not found and pack it)
object DMJsonProtocol extends DefaultJsonProtocol {

   implicit object DMValueJsonFormat extends RootJsonFormat[DMValue] {
      override def write(m: DMValue): JsValue = m match {
         case DMNull => JsNull
         case DMError(message) => throw new SerializationException("Error is not serializable")
         case DMInteger(value) => JsNumber(value)
         case DMString(value) => JsString(value)
         case DMDate(value) => JsObject(
               "type" -> JsString("date"),
               "data" -> dateToStr(value)
         )
         case DMArray(values) => JsArray(values.map(write))
         case DMStruct(values, computables) => JsObject( // TODO computables
            "type" -> JsString("struct"),
            "data" -> JsObject(values.mapValues(write))
         )
         case DMYearDate(value) => JsObject(
            "type" -> JsString("yeardate"),
            "data" -> value.fold(JsNumber(_), dateToStr)
         )
      }

      private def dateToStr(d: DMValue.Date): JsString = JsString(DateHelper.toString(d))

      private def readComplex(name: String, data: JsValue): DMValue = (name, data) match {
         case ("date", JsString(s)) => DMDate(DateHelper.fromString(s))
         case ("yeardate", JsString(s)) => DMYearDate(DateHelper.fromString(s))
         case ("yeardate", JsNumber(x)) => DMYearDate(x.toInt)
         case ("struct", JsObject(fields)) => DMStruct(fields.mapValues(read), Map.empty) // TODO struct computables
         case _ => throw DeserializationException("Unsupported data format")
      }

      override def read(json: JsValue): DMValue = json match {
         case JsObject(fields) => fields("type") match {
            case JsString(name) => readComplex(name, fields("data"))
            case _ => throw DeserializationException("Unsupported data format")
         }
         case JsArray(elements) => DMArray(elements.map(read).toVector)
         case JsString(value) => DMString(value)
         case JsNumber(value) => DMInteger(value.toInt)
         case _: JsBoolean => throw DeserializationException("Unsupported data format")
         case JsNull => DMNull
      }
   }

}
