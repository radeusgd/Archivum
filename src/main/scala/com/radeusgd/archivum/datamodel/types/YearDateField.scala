package com.radeusgd.archivum.datamodel.types

import java.time.LocalDate

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import com.radeusgd.archivum.utils.AsInt
import spray.json.{JsNull, JsString, JsValue}

object YearDateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMError(msg) => ConstraintError(Nil, msg) :: Nil
         case DMNull => Nil
         case DMYearDate(_) => Nil
         case _ => TypeError(Nil, v.toString, "DMYearDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   private val reprYearLength = 4
   private val reprMonthLength = 2
   private val reprDayLength = 2
   private val reprLength = reprYearLength + reprMonthLength + reprDayLength

   def dateToRepr(d: DMValue): String = d match {
      case DMNull => ""
      case DMYearDate(value) =>
         value match {
            case Right(date) =>
               val y = date.getYear
               val m = date.getMonth.getValue
               val d = date.getDayOfMonth
               f"$y%04d$m%02d$d%02d"
            case Left(year) =>
               f"$year%04d"
         }
      case _ => throw new IllegalArgumentException
   }

   def dateFromRepr(s: String): DMValue = {
      val y = s.slice(0, reprYearLength)
      val m = s.slice(reprYearLength, reprYearLength + reprMonthLength)
      val d = s.slice(reprYearLength + reprMonthLength, reprLength)
      (y, m, d) match {
         case ("", "", "") => DMNull
         case (AsInt(year), "", "") => DMYearDate(year)
         case (AsInt(year), AsInt(month), AsInt(day)) => DMYearDate(LocalDate.of(year, month, day))
         case _ => throw new RuntimeException("Internal serialization error. This certainly shouldn't happen.")
      }
   }

   override def tableSetup(path: Seq[String], table: Setup): Unit =
      table.addField(path, DBTypes.ShortString(reprLength))

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      val s = table.getString(path)
      dateFromRepr(s)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      val s = dateToRepr(value)
      table.setValue(path, s)
   }

   override def toHumanJson(v: DMValue): JsValue = v match {
      case d: DMYearDate => JsString(d.toString) // TODO ?
      case DMNull => JsNull
      case _ => throw new IllegalArgumentException
   }

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = ??? // TODO
}
