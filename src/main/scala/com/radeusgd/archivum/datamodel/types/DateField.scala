package com.radeusgd.archivum.datamodel.types

import java.time.LocalDate

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import com.radeusgd.archivum.utils.AsInt
import spray.json.{DeserializationException, JsNull, JsString, JsValue}

object DateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMError(msg) => ConstraintError(Nil, msg) :: Nil
         case DMNull => Nil
         case DMDate(date) => if (date.getYear < 1583 || date.getYear > 4000) ConstraintError(Nil, "This date is not in the supported range") :: Nil else Nil
         case _ => TypeError(Nil, v.toString, "DMDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   private val reprYearLength = 4
   private val reprMonthLength = 2
   private val reprDayLength = 2
   private val reprLength = reprYearLength + reprMonthLength + reprDayLength

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      // we use string for dates because SQL dates seem to support only XX century and forward
      table.addField(path, DBTypes.ShortString(reprLength))
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      val s = table.getString(path)
      dateFromRepr(s)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      val s = dateToRepr(value)
      table.setValue(path, s)
   }

   override def toHumanJson(v: DMValue): JsValue = v match {
      case DMDate(date) => JsString(date.toString)
      case DMNull => JsNull
      case _ => throw new IllegalArgumentException
   }

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = j match {
      case JsString(s) => Right(DMDate(LocalDate.parse(s)))
      case _ => Left(DeserializationException("Expected a date string"))
   }

   def dateFromRepr(s: String): DMValue = {
      val y = s.slice(0, reprYearLength)
      val m = s.slice(reprYearLength, reprYearLength + reprMonthLength)
      val d = s.slice(reprYearLength + reprMonthLength, reprLength)
      (y, m, d) match {
         case ("", "", "") => DMNull
         case (AsInt(year), AsInt(month), AsInt(day)) => DMDate(LocalDate.of(year, month, day))
         case _ => throw new RuntimeException("Internal serialization error. This certainly shouldn't happen.")
      }
   }

   def dateToRepr(d: DMValue): String = d match {
      case DMNull => ""
      case DMDate(date) =>
         val y = date.getYear.toString
         val m = date.getMonth.getValue
         val d = date.getDayOfMonth.toString
         s"$y%04d$m%02d$d%02d"
      case _ => throw new IllegalArgumentException
   }
}