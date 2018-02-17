package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types._
import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[Model] {
      override def write(obj: Model): JsValue = {
         throw new NotImplementedError("Currently serializing model definition back to JSON is not supported.")
      }

      override def read(json: JsValue): Model = {
         json.asJsObject.getFields("name", "types", "fields") match {
            case Seq(JsString(name), JsObject(types), JsObject(fields)) =>
               val customTypes: Map[String, FieldType] = types map (readCustomTypeDef _).tupled
               new Model(name, readStructDef(customTypes)(fields))
            case _ => throw DeserializationException("Wrong model root structure")
         }
      }
   }

   private def readStructDef(customTypes: Map[String, FieldType])(fields: Map[String, JsValue]): StructField =
      StructField(fields mapValues readFieldType(customTypes))

   private def readCustomTypeDef(name: String, json: JsValue): (String, FieldType) = {
      json match {
         case JsArray(_)  => name -> new EnumField(json.convertTo[IndexedSeq[String]])
            /*
             TODO for now custom types cannot use other custom types
             (ie. custom struct cannot use enum), but it can easily be fixed
             */
         case JsObject(fields) => name -> readStructDef(Map.empty[String, FieldType])(fields)
         case _ => throw DeserializationException("Expected an enum or struct definition")
      }
   }

   private val arrayPrefixed = """^array:(.*)""".r

   private def readFieldType(customTypes: Map[String, FieldType])(json: JsValue): FieldType = {
      json match {
         case JsString(arrayPrefixed(suf)) => ArrayField(readFieldType(customTypes)(JsString(suf)))
         case JsString("string") => StringField
         case JsString("bigtext") => StringField
         case JsString("date") => DateField
         case JsString(typename) => customTypes(typename)
         case _ => throw DeserializationException("Expected a typename")
      }
   }
}
