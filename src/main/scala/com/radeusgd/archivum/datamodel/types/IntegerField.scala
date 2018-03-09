package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import com.radeusgd.archivum.utils.AsInt
import spray.json.{DeserializationException, JsNull, JsNumber, JsString, JsValue}

object IntegerField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMError(msg) => ConstraintError(Nil, msg) :: Nil
         case DMNull => Nil
         case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.toString, "DMInteger") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.Integer)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue =
      table.getInt(path).map(DMInteger).getOrElse(DMNull)

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      //noinspection ScalaStyle
      value match {
         case DMInteger(i) => table.setValue(path, i)
         case DMNull => table.setValue(path, null)
         case _ => throw new IllegalArgumentException
      }
   }


   override def toHumanJson(v: DMValue): JsValue = v match {
      case DMInteger(x) => JsNumber(x)
      case DMNull => JsNull
      case _ => throw new IllegalArgumentException
   }

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = j match {
      case JsNumber(x) => Right(DMInteger(x.toInt))
      case JsNull => Right(DMNull)
      case JsString(AsInt(x)) => Right(DMInteger(x)) // allow for conversion from string
      case _ => Left(DeserializationException("Expected an integer"))
   }
}
