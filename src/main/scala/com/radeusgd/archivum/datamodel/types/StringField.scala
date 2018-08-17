package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import spray.json.{DeserializationException, JsNumber, JsString, JsValue}

object StringField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(_) => Nil
         case _ => TypeError(Nil, v.toString, "DMString") :: Nil
      }

   override def makeEmpty: DMValue = DMString("")

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.String, indexed = true)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = DMString(table.getString(path))

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      //noinspection ScalaStyle
      value match {
         case DMString(str) => table.setValue(path, str)
         case DMNull => table.setValue(path, null)
         case _ => assert(false)
      }
   }

   override def toHumanJson(v: DMValue): JsValue = v.asString.map(JsString(_)).get

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = j match {
      case JsString(s) => Right(DMString(s))
      case JsNumber(x) => Right(DMString(x.toString)) // allow for conversions
      case _ => Left(DeserializationException("Expected a string"))
   }
}
