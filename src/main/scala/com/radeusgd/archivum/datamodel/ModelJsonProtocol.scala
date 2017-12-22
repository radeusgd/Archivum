package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types.{FieldType, SimpleString, EnumType}
import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[ModelDefinition] {
      override def write(obj: ModelDefinition): JsValue = ???
      override def read(json: JsValue): ModelDefinition = {
         json.asJsObject.getFields("name", "types", "fields") match {
            case Seq(JsString(name), JsObject(types), JsObject(fields)) =>
               val customTypes: Map[String, FieldType] = types map (readEnumDef _).tupled
               new ModelDefinition(name, fields mapValues readField(customTypes))
            case _ => throw DeserializationException("Wrong model root structure")
         }
      }
   }

   private def readEnumDef(name: String, json: JsValue): (String, FieldType) = {
      json match {
         case JsArray(_)  => name -> new EnumType(json.convertTo[IndexedSeq[String]])
         case _ => throw DeserializationException("Expected an enum definition")
      }
   }

   private def readField(customTypes: Map[String, FieldType])(json: JsValue): FieldType = {
      json match {
         case JsString("string") => SimpleString
         case JsString("date") => ???
         case JsString(typename) => customTypes(typename)
         case _ => throw DeserializationException("Expected typename")
      }
   }
  /*implicit object ColorJsonFormat extends RootJsonFormat[Color] {
    def write(c: Color) = JsObject(
      "name" -> JsString(c.name),
      "red" -> JsNumber(c.red),
      "green" -> JsNumber(c.green),
      "blue" -> JsNumber(c.blue)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("name", "red", "green", "blue") match {
        case Seq(JsString(name), JsNumber(red), JsNumber(green), JsNumber(blue)) =>
          new Color(name, red.toInt, green.toInt, blue.toInt)
        case _ => throw new DeserializationException("Color expected")
      }
    }
  }*/
}
