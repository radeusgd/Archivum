package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

object IntegerField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMError(msg) => ConstraintError(Nil, msg) :: Nil
         case DMNull => Nil
         case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.toString, "DMInteger") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.Integer)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      table.getField(path, DBTypes.Integer)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      //noinspection ScalaStyle
      value match {
         case DMInteger(i) => table.setValue(path, i)
         case DMNull => table.setValue(path, null)
         case _ => assert(false)
      }
   }
}
