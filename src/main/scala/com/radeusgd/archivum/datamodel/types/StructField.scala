package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

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

            unknownErrors ++ childErrors
         case _ => TypeError(Nil, v.toString, "DMStruct") :: Nil
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

   override def tableFetch(path: Seq[String], table: Fetch): DMStruct = {
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
