package com.radeusgd.archivum.utils

import java.time.LocalDate

import com.radeusgd.archivum.datamodel.DMValue

object ClassicDate {
   private val AsClassicDate = raw"(\d{2})-(\d{2})-(\d{4})".r

   def apply(date: DMValue.Date): String =
      f"${date.getDayOfMonth}%02d-${date.getMonth.getValue}%02d-${date.getYear}%04d"

   def unapply(str: String): Option[DMValue.Date] = str match {
      case AsClassicDate(AsInt(day), AsInt(month), AsInt(year)) =>
         util.Try(LocalDate.of(year, month, day)).toOption
      case _ => None
   }

}
