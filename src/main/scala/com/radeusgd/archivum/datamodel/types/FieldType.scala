package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel.{DMValue, ValidationError}
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import spray.json.JsValue

trait FieldType {
   def validate(v: DMValue): List[ValidationError]

   def makeEmpty: DMValue

   def tableSetup(path: Seq[String], table: Setup): Unit

   def tableFetch(path: Seq[String], table: Fetch): DMValue

   def tableInsert(path: Seq[String], table: Insert, value: DMValue)

   def toHumanJson(v: DMValue): JsValue

   // TODO possibly change to Validated, and change Throwable to some sealed type
   def fromHumanJson(j: JsValue): Either[Throwable, DMValue]
}