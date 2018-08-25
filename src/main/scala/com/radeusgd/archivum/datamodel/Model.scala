package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.ModelJsonProtocol._
import com.radeusgd.archivum.datamodel.types._
import spray.json.JsonParser

import scala.util.Try

case class Model(name: String, roottype: StructField) {
   def makeEmpty: DMStruct = roottype.makeEmpty
}

object Model {
   def fromDefinition(definition: String): Try[Model] = {
      util.Try({
         val json = JsonParser(removeComments(definition))
         json.convertTo[Model]
      })
   }

   private def removeComments(definition: String): String =
      definition.split("\n").map(line => line.takeWhile(_ != '#')).mkString("\n")
}