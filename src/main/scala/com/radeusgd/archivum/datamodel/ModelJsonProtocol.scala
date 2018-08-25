package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types._
import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {

   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[Model] {
      override def write(m: Model): JsValue = {
         //val types: Map[String, FieldType]
         JsObject(
            "name" -> JsString(m.name),
            "types" -> ???,
            "fields" -> ???
         )
      }

      override def read(json: JsValue): Model = {
         json.asJsObject.getFields("name", "types", "fields") match {
            case Seq(JsString(name), JsArray(types), JsObject(fields)) =>
               val customTypes: Map[String, FieldType] = parseCustomTypes(types.map(_.asJsObject("Each type should be a JSON object")))
               println(customTypes.keys)
               new Model(name, readStructDef(customTypes)(fields))
            case _ => throw DeserializationException("Wrong model root structure")
         }
      }
   }

   private def readStructDef(customTypes: Map[String, FieldType])(fields: Map[String, JsValue]): StructField =
      StructField(fields.mapValues(readFieldType(customTypes)))

   private def parseCustomTypes(typeDefs: Vector[JsObject]): Map[String, FieldType] =
      typeDefs.foldLeft(Map.empty[String, FieldType])(
         (soFar: Map[String, FieldType], typedef: JsObject) => {
            val JsString(key) = typedef.fields("name")
            val definition = typedef.fields("def")
            soFar.updated(key, readCustomTypeDef(soFar, definition))
         })

   private def readCustomTypeDef(customTypesSoFar: Map[String, FieldType], json: JsValue): FieldType = {
      json match {
         case JsArray(_) => new EnumField(json.convertTo[IndexedSeq[String]])
         case JsObject(fields) => readStructDef(customTypesSoFar)(fields)
         case _ => throw DeserializationException("Expected an enum or struct definition")
      }
   }

   private val arrayPrefixed = """^array:(.*)""".r

   private def readFieldType(customTypes: Map[String, FieldType])(json: JsValue): FieldType = {
      json match {
         case JsString(arrayPrefixed(suf)) => ArrayField(readFieldType(customTypes)(JsString(suf)))
         case JsString("text") => StringField
         case JsString("bigtext") => StringField
         case JsString("integer") => IntegerField
         case JsString("date") => DateField
         case JsString("yeardate") => YearDateField
         case JsString(typename) =>
            println(typename)
            println(customTypes.keySet)
            customTypes(typename)
         case _ => throw DeserializationException("Expected a typename")
      }
   }
}
