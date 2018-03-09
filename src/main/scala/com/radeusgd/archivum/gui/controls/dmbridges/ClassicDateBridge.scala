package com.radeusgd.archivum.gui.controls.dmbridges
import java.time.LocalDate

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.datamodel.types.DateField
import com.radeusgd.archivum.utils.AsInt

object ClassicDateBridge extends StringDMBridge {
   private val AsClassicDate = raw"(\d{2})-(\d{2})-(\d{4})".r

   override def fromString(s: String): DMValue = s match {
      case "" => DMNull
      case AsClassicDate(AsInt(day), AsInt(month), AsInt(year)) =>
         val d = util.Try(LocalDate.of(year, month, day)).toOption.toRight("Incorrect format")
         d.flatMap(DMDate.safe(_).toRight("Date is not in supported range"))
            .fold(DMError, identity)
      case _ => DMError("Incorrect date format")
   }

   override def fromDM(v: DMValue): String = v match {
      case DMDate(date) => f"${date.getDayOfMonth}%d-${date.getMonth.getValue}%d-${date.getYear}%d"
      case DMNull => ""
      case _ => throw new IllegalArgumentException
   }
}
