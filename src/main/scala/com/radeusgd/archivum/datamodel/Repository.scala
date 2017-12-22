package com.radeusgd.archivum.datamodel

class Repository(val definition: ModelDefinition) {

}

import spray.json._
import ModelJsonProtocol._

object Repository {
  def open(filename: String): Repository = {
    val source = io.Source.fromFile(filename)
    val text = try source.getLines mkString "\n" finally source.close()
    val modelJsonAst = text.parseJson
    val defn = modelJsonAst.convertTo[ModelDefinition]
    new Repository(defn)
  }
}
