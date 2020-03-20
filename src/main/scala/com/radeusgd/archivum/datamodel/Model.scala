package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.ModelJsonProtocol._
import com.radeusgd.archivum.datamodel.dmbridges.{ClassicDateBridge, ClassicYearDateBridge, EnumBridge, IntegerBridge, StringBridge, StringDMBridge}
import com.radeusgd.archivum.datamodel.types._
import spray.json.JsonParser

import scala.util.Try

case class Model(name: String, roottype: StructField) {
   // returns an empty object conforming to the model
   def makeEmpty: DMStruct = roottype.makeEmpty

   // tries to infer a default String-DM Bridge for the object under the path
   // returns None if no bridge can be inferred (bridges are not supported for composites like arrays and structs)
   def defaultBridgeForField(path: List[String]): Option[StringDMBridge] = {
      val typ = roottype.getType(path)
      typ match {
         case DateField => Some(ClassicDateBridge)
         case StringField => Some(StringBridge)
         case enum: EnumField => Some(EnumBridge(enum))
         case IntegerField => Some(IntegerBridge)
         case YearDateField => Some(ClassicYearDateBridge)
         case _ => None
      }
   }
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