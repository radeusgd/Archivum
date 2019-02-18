package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import spray.json.{DeserializationException, JsObject, JsValue}

// TODO computable fields
case class StructField(fieldTypes: Map[String, FieldType]) extends FieldType with TypedAggregateField {
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
         case next :: rest => fieldTypes(next).asInstanceOf[TypedAggregateField].getType(rest)
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

   override def toHumanJson(v: DMValue): JsValue = {
      val obj = v.asInstanceOf[DMStruct]
      JsObject(obj.values.map { case (name, value) => (name, fieldTypes(name).toHumanJson(value)) } )
   }

   private def fieldFromHumanJson(name: String, value: JsValue): Either[Throwable, DMValue] =
      fieldTypes.get(name)
         .toRight(DeserializationException("Field " + name + " unknown"))
         .flatMap(_.fromHumanJson(value))

   override def fromHumanJson(j: JsValue): Either[Throwable, DMStruct] = j match {
      case JsObject(fields) =>
         val values: Map[String, Either[Throwable, DMValue]] = fields.transform(fieldFromHumanJson)
         // this is slightly hacky:
         util.Try(values.mapValues(_.fold(throw _, identity))).toEither
            .map(DMStruct(_))
      case _ => Left(DeserializationException("Expected an object"))
   }

   def extractAllStrings(s: DMStruct): Seq[String] = {
      fieldTypes.flatMap {
         case (key, struct: StructField) =>
            struct.extractAllStrings(s.values(key).asInstanceOf[DMStruct])
         case (key, ArrayField(arraystruct: StructField)) =>
            s.values(key).asInstanceOf[DMArray].values.flatMap(d => arraystruct.extractAllStrings(d.asInstanceOf[DMStruct]))
         case (key, ArrayField(StringField)) =>
            s.values(key).asInstanceOf[DMArray].values.map(d => d.asInstanceOf[DMString].value)
         case (key, StringField) =>
            Seq(s.values(key).asInstanceOf[DMString].value)
         case _ =>
            Seq()
      }.toSeq
   }
}
