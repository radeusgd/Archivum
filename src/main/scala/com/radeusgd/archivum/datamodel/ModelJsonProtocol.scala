package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types.{EnumType, FieldType}
import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[Model] {
      override def write(obj: Model): JsValue = {
         throw new NotImplementedError("Currently serializing model definition back to JSON is not supported.")
      }

      override def read(json: JsValue): Model = {
         json.asJsObject.getFields("name", "types", "fields") match {
            case Seq(JsString(name), JsObject(types), JsObject(fields)) =>
               val customTypes: Map[String, FieldType] = types map (readEnumDef _).tupled
               new Model(name, fields mapValues readField(customTypes))
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
         case JsString("string") => null//SimpleString
         case JsString("bigtext") => null//LongString
         case JsString("date") => null//SimpleString // TODO! FIME
         case JsString(typename) => customTypes(typename)
         case _ => throw DeserializationException("Expected typename")
      }
   }
}
