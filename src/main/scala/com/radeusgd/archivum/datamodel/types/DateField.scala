package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import spray.json.JsValue

object DateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMError(msg) => ConstraintError(Nil, msg) :: Nil
         case DMNull => Nil
         case DMDate(_) => Nil
         case _ => TypeError(Nil, v.toString, "DMDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      ???
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      ???
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      ???
   }

   override def toHumanJson(v: DMValue): JsValue = ???

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = ???
}
