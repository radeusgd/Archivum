package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel.{DMValue, ValidationError}
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}

trait FieldType {
   def validate(v: DMValue): List[ValidationError]

   def makeEmpty: DMValue

   def tableSetup(path: Seq[String], table: Setup): Unit

   def tableFetch(path: Seq[String], table: Fetch): DMValue

   def tableInsert(path: Seq[String], table: Insert, value: DMValue)
}