package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

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

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.String)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      val res = table.getField(path, DBTypes.String)
      assert(validate(res).isEmpty)
      res
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      value match {
         case DMString(str) => table.setValue(path, str)
         case _ => assert(false)
      }
   }
}