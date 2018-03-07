package com.radeusgd.archivum.datamodel

import java.time.LocalDate

import com.radeusgd.archivum.utils.AsInt

object DateHelper {
   // TODO make sure these work correctly
   private val date = raw"(\d{4})-(\d{2})-(\d{2})".r
   def fromString(s: String): DMValue.Date = s match {
      case date(AsInt(year), AsInt(month), AsInt(day)) => LocalDate.of(year, month, day)
      case _ => throw new IllegalArgumentException(s + " is not a correct date")
   }

   def toString(d: DMValue.Date): String = d.toString
}
