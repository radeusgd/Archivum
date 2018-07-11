package com.radeusgd.archivum.datamodel

import java.time.LocalDate

import com.radeusgd.archivum.datamodel.types.DateField
import com.radeusgd.archivum.languages.QueryLanguage
import com.radeusgd.archivum.utils.AsInt

sealed abstract class DMValue {
   // TODO not sure if they'll be useful
   def asInt: Option[Int] = None

   def asString: Option[String] = None

   def asDate: Option[DMValue.Date] = None

   //def asArray: DMArray = throw new ClassCastException
   //def asStruct: DMStruct = throw new ClassCastException
   // TODO if above are staying, add asYearDate
}

object DMValue {
   type Date = LocalDate
}

trait DMAggregate extends DMValue with ((String) => DMValue) {
   def updated(key: String, v: DMValue): DMAggregate
}

trait DMOrdered extends Ordered[DMOrdered]

class IncompatibleTypeComparison extends IllegalArgumentException

object DMNull extends DMValue {
   override def toString: String = "NULL"
}

case class DMError(message: String) extends DMValue

case class DMInteger(value: Int) extends DMValue with DMOrdered {
   override def asInt: Option[Int] = Some(value)

   override def toString: String = value.toString

   override def compare(that: DMOrdered): Int =
      that match {
         case DMInteger(thatvalue) => value compare thatvalue
         case _ => throw new IncompatibleTypeComparison
      }
}

case class DMString(value: String) extends DMValue with DMOrdered {
   override def asString: Option[String] = Some(value)

   override def toString: String = value

   override def compare(that: DMOrdered): Int =
      that match {
         case DMString(thatvalue) => value compare thatvalue
         case _ => throw new IncompatibleTypeComparison
      }
}

case class DMDate(value: DMValue.Date) extends DMValue with DMOrdered {
   override def asDate: Option[DMValue.Date] = Some(value)

   override def toString: String = value.toString

   override def compare(that: DMOrdered): Int =
      that match {
         case DMDate(thatvalue) => value compareTo thatvalue
         case _ => throw new IncompatibleTypeComparison
      }
}

object DMDate {
   def safe(v: DMValue.Date): Option[DMDate] = if (DateField.isDateSupported(v)) Some(DMDate(v)) else None
}

case class DMArray(values: Vector[DMValue]) extends DMValue with DMAggregate {
   override def toString: String = "DMArray(" + values.map(_.toString).mkString(", ") + ")"
   def length: Int = values.length

   def updated(idx: Int, v: DMValue): DMArray = DMArray(values.updated(idx, v))
   override def updated(key: String, v: DMValue): DMAggregate = // TODO safety
      updated(key.toInt, v)

   override def apply(s: String): DMValue =
      s match {
         case QueryLanguage.ArrayLength => DMInteger(values.length)
         case AsInt(ind) /*if ind >= 0 && ind < values.length*/ => values(ind) // TODO not sure what to throw here
         case _ => throw new NoSuchFieldException
      }

   def apply(idx: Int): DMValue = values(idx)

   def appended(v: DMValue): DMArray = DMArray(values ++ Vector(v))

   def without(idx: Int): DMArray = DMArray(values.take(idx) ++ values.drop(idx + 1))
}

// this computables format is deprecated
case class DMStruct(values: Map[String, DMValue],
                    @deprecated computables: Map[String, (DMStruct) => DMValue] = Map.empty)
   extends DMValue with DMAggregate {
   override def toString: String = values.toString() // TODO better toString, also include computables

   def updated(key: String, v: DMValue): DMStruct = DMStruct(values.updated(key, v), computables)

   override def apply(s: String): DMValue =
      values.getOrElse(s, computables(s)(this)) // if s is present in values computables(s) is not actually evaluated
}

case class DMYearDate(value: Either[Int, DMValue.Date]) extends DMValue with DMAggregate with DMOrdered {
   override def asDate: Option[DMValue.Date] = fullDate

   def year: Int = value.fold(identity, _.getYear)

   def fullDate: Option[DMValue.Date] = value.toOption

   override def apply(s: String): DMValue =
      s match {
         case QueryLanguage.YDYear => DMInteger(year)
         case QueryLanguage.YDFullDate => value.fold(_ => DMNull, DMDate(_))
      }

   override def toString: String = value.fold(_.toString, _.toString)

   override def compare(that: DMOrdered): Int =
      that match {
         case thatd: DMYearDate => year compare thatd.year
         case _ => throw new IncompatibleTypeComparison
      }

   override def updated(key: String, v: DMValue): DMAggregate = ???
}

object DMYearDate {
   def apply(onlyYear: Int): DMYearDate = DMYearDate(Left(onlyYear))

   def apply(fullDate: DMValue.Date): DMYearDate = DMYearDate(Right(fullDate))
}