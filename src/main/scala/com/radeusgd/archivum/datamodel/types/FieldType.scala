package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

trait FieldType {
   def validate(v: DMValue): List[ValidationError]

   def makeEmpty: DMValue

   def tableSetup(path: Seq[String], table: Setup): Unit
   def tableFetch(path: Seq[String], table: Fetch): DMValue
   def tableInsert(path: Seq[String], table: Insert, value: DMValue)
}

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
      value match {
         case DMString(str) => table.setValue(path, str)
         case DMNull => table.setValue(path, null)
         case _ => assert(false)
      }
   }
}

object IntegerField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMInteger") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      table.addField(path, DBTypes.Integer)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      table.getField(path, DBTypes.Integer)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      value match {
         case DMInteger(i) => table.setValue(path, i)
         case DMNull => table.setValue(path, null)
         case _ => assert(false)
      }
   }
}

object DateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMDate(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      ???
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      ???
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      ???
   }
}

object YearDateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMYearDate(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMYearDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull

   override def tableSetup(path: Seq[String], table: Setup): Unit = ???

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = ???

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = ???
}

// TODO computable fields
case class StructField(fieldTypes: Map[String, FieldType]) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMStruct(values, _) =>
            val unknown = values.keySet -- fieldTypes.keySet
            val unknownErrors: List[ValidationError] = unknown.toList map { key => ConstraintError(Nil, key + " is not expected in this struct") }

            val childErrors: List[ValidationError] =
               fieldTypes.toList flatMap { case (name, ft) =>
                  values.get(name) match {
                     case Some(vv) => ft.validate(vv) map {
                        _.extendPath(name)
                     }
                     case None => ConstraintError(Nil, name + " is missing") :: Nil
                  }
               }

            unknownErrors ++ childErrors.toList
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMStruct") :: Nil
      }

   def getType(path: List[String]): FieldType = // TODO option/either
      path match {
         case Nil => this
         case last :: Nil => fieldTypes(last)
         case next :: rest => fieldTypes(next).asInstanceOf[StructField].getType(rest) // TODO FIXME add array support and error checking
      }

   override def makeEmpty: DMStruct = DMStruct(fieldTypes mapValues (_.makeEmpty), Map.empty)

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      for ((name, ft) <- fieldTypes) {
         ft.tableSetup(path ++ List(name), table)
      }
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      val fields = fieldTypes map { case (name, ft) => (name, ft.tableFetch(path ++ List(name), table)) }
      DMStruct(fields) // TODO computable fields
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      val obj: DMStruct = value.asInstanceOf[DMStruct]
      for ((name, ft) <- fieldTypes) {
         ft.tableInsert(path ++ List(name), table, obj(name))
      }
   }
}

case class ArrayField(elementsType: FieldType) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMArray(values) => {
            val indexedValues: Seq[(DMValue, Int)] = values.zipWithIndex
            val childErrors: Seq[ValidationError] =
               indexedValues flatMap { case (vv, ind) =>
                  elementsType.validate(vv) map {
                     _.extendPath(ind.toString)
                  }
               }

            childErrors.toList
         }
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMArray") :: Nil
      }

   override def makeEmpty: DMArray = DMArray(Vector.empty)

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      val sub = table.addSubTable(path)
      elementsType.tableSetup(Nil, sub)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue = {
      val subs = table.getSubTable(path)
      val values = subs map { (sub) => elementsType.tableFetch(Nil, sub) }
      DMArray(values.toVector)
   }

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      val arr: DMArray = value.asInstanceOf[DMArray]
      val subs = table.setSubTable(path, arr.length)
      val vAndSub: Seq[(Insert, DMValue)] = subs.zip(arr.values)
      vAndSub.foreach({ case (sub, svalue) => elementsType.tableInsert(Nil, sub, svalue)})
   }
}

// TODO Image Field (uses DMString for content address and manual content handling)
