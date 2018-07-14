package com.radeusgd.archivum.gui.controls.dmbridges

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.utils.ClassicDate

object ClassicDateBridge extends StringDMBridge {

   override def fromString(s: String): DMValue = s match {
      case "" => DMNull
      case ClassicDate(date) =>
         DMDate.safe(date).getOrElse(DMError("Date is not in supported range"))
      case _ => DMError("Incorrect date format")
   }

   override def fromDM(v: DMValue): String = v match {
      case DMDate(date) => ClassicDate(date)
      case DMNull => ""
      case _ => throw new IllegalArgumentException
   }
}
