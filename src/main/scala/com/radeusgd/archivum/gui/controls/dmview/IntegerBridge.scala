package com.radeusgd.archivum.gui.controls.dmview
import com.radeusgd.archivum.datamodel.{DMError, DMInteger, DMNull, DMValue}
import com.radeusgd.archivum.utils.AsInt

object IntegerBridge extends StringDMBridge {
   override def fromString(s: String): DMValue = s match {
      case "" => DMNull
      case AsInt(x) => DMInteger(x)
      case _ => DMError(s + " is not a valid integer")
   }

   override def fromDM(v: DMValue): String = v match {
      case DMNull => ""
      case DMInteger(x) => x.toString
      case _ => throw new IllegalArgumentException
   }
}
