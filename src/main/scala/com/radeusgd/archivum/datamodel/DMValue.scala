package com.radeusgd.archivum.datamodel

import java.time.LocalDate

import com.radeusgd.archivum.utils.AsInt

sealed class DMValue {
   // TODO not sure if they'll be useful
   def asInt: DMInteger = throw new ClassCastException
   def asString: DMString = throw new ClassCastException
   def asDate: DMDate = throw new ClassCastException
   def asArray: DMArray = throw new ClassCastException
   def asStruct: DMStruct = throw new ClassCastException
   // TODO if above are staying, add asYearDate
}

object DMValue {
   type Date = LocalDate
}

trait DMAggregate extends ((String) => DMValue)

object DMNull extends DMValue

case class DMInteger(value: Int) extends DMValue {
   override def asInt: DMInteger = this
}

case class DMString(value: String) extends DMValue {
   override def asString: DMString = this
}

// TODO  FIXME  historic dates support!
case class DMDate(value: DMValue.Date) extends DMValue {
   override def asDate: DMDate = this
}

case class DMArray(values: Vector[DMValue]) extends DMValue with DMAggregate {
   override def asArray: DMArray = this
   def updated(idx: Int, v: DMValue): DMArray = DMArray(values.updated(idx, v))

   override def apply(s: String): DMValue =
      s match {
         case "length" => DMInteger(values.length)
         case AsInt(ind) /*if ind >= 0 && ind < values.length*/ => values(ind) // TODO not sure what to throw here
         case _ => throw new NoSuchFieldException
      }
}

case class DMStruct(values: Map[String, DMValue],
                    computables: Map[String, (DMStruct) => DMValue] = Map.empty)
   extends DMValue with DMAggregate {
   override def asStruct: DMStruct = this
   def updated(key: String, v: DMValue) = DMStruct(values.updated(key, v), computables)

   override def apply(s: String): DMValue =
      values.getOrElse(s, computables(s)(this)) // if s is present in values computables(s) is not actually evaluated
}

case class DMYearDate(value: Either[Int, DMValue.Date]) extends DMValue with DMAggregate {
   def year: Int = value.fold(identity, _.getYear)
   def fullDate: Option[DMValue.Date] = value.toOption

   override def apply(s: String): DMValue =
      s match {
         case "year" => DMInteger(year)
         case "date" => value.fold(_ => DMNull, DMDate(_))
      }
}

object DMYearDate {
   def apply(onlyYear: Int): DMYearDate = DMYearDate(Left(onlyYear))
   def apply(fullDate: DMValue.Date): DMYearDate = DMYearDate(Right(fullDate))
}