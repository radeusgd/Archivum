package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel.{DMString, DMValue, TypeError, ValidationError}

trait FieldType {
   def validate(v: DMValue): List[ValidationError]
}

object String extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName,"DMString") :: Nil
      }
}