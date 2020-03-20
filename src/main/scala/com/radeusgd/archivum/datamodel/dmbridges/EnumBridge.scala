package com.radeusgd.archivum.datamodel.dmbridges

import com.radeusgd.archivum.datamodel.{DMError, DMString, DMValue, ValidationError}
import com.radeusgd.archivum.datamodel.types.EnumField

case class EnumBridge(typ: EnumField) extends StringDMBridge {
   override def fromString(s: String): DMValue = {
      val v = DMString(s)
      val errors = typ.validate(v)
      if (errors.isEmpty) v
      else DMError(ValidationError.describe(errors))
   }

   override def fromDM(v: DMValue): String = v.toString
}
