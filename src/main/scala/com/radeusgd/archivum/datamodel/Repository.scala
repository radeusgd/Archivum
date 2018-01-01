package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types.FieldType

class Repository(val definition: ModelDefinition) {
   def newInstance(): ModelInstance = {
      val typesIntoInstances = (name: String, fieldType: FieldType) => (name, fieldType.createEmptyField(name))
      new ModelInstance(definition.fields map typesIntoInstances.tupled)
   }
}

import com.radeusgd.archivum.datamodel.ModelJsonProtocol._
import spray.json._

object Repository {
   def open(filename: String): Repository = {
      val source = io.Source.fromFile(filename)
      val text = try source.getLines mkString "\n" finally source.close()
      val modelJsonAst = text.parseJson
      val defn = modelJsonAst.convertTo[ModelDefinition]
      new Repository(defn)
   }
}
