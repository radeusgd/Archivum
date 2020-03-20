package com.radeusgd.archivum.datamodel.dmbridges
import com.radeusgd.archivum.datamodel.{DMString, DMValue}

object StringBridge extends StringDMBridge {
   override def fromString(s: String): DMValue = DMString(s)

   override def fromDM(v: DMValue): String = v.asString.get
}
