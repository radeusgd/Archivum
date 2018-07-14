package com.radeusgd.archivum.gui.controls.dmbridges

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.utils.{AsInt, ClassicDate}

object ClassicYearDateBridge extends StringDMBridge {
   override def fromString(s: String): DMValue = s match {
      case "" => DMNull
      case AsInt(x) => DMYearDate(x)
      case ClassicDate(date) => // TODO FIXME check range
         DMYearDate(date)
      case _ => DMError("Wrong year/date format")
   }

   override def fromDM(v: DMValue): String = v match {
      case DMNull => ""
      case yd: DMYearDate =>
         yd.asDate.map(d => ClassicDate(d)).getOrElse(yd.year.toString)
      case _ => throw new IllegalArgumentException
   }
}
