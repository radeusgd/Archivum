package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.ModelJsonProtocol._
import com.radeusgd.archivum.datamodel.types._
import spray.json.JsonParser

import scala.util.Try

case class Model(name: String, roottype: StructField)

object Model {
   def fromDefinition(definition: String): Try[Model] = {
      util.Try({
         val json = JsonParser(definition)
         json.convertTo[Model]
      })
   }
}