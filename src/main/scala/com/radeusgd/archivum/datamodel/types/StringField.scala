package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

object StringField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMString") :: Nil
      }

   override def makeEmpty: DMValue = DMString("")

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.String)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      table.getField(path, DBTypes.String)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      //noinspection ScalaStyle
      value match {
         case DMString(str) => table.setValue(path, str)
         case DMNull => table.setValue(path, null)
         case _ => assert(false)
      }
   }
}
