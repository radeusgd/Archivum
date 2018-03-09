package com.radeusgd.archivum.gui.controls.dmbridges
import com.radeusgd.archivum.datamodel._

object DateBridge extends StringDMBridge {
   override def fromString(s: String): DMValue = {
      if (s == "") DMNull
      else DateHelper.fromStringOpt(s).flatMap(DMDate.safe).getOrElse(DMError("Wrong date format"))
   }

   override def fromDM(v: DMValue): String = v match {
      case DMDate(date) => date.toString
      case DMNull => ""
      case _ => throw new IllegalArgumentException
   }
}
