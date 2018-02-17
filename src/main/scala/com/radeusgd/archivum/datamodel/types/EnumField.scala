package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._

class EnumField(val values: IndexedSeq[String]) extends FieldType {
   //override def createEmptyField(name: String): Field = new EnumField(this, name, values(0))


   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(str) =>
            if (values.contains(str)) Nil
            else ConstraintError(Nil, str + " is not a valid value for this field") :: Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMString") :: Nil
      }

   override def makeEmpty: DMValue = DMString(values.head)
}