package com.radeusgd.archivum.search

case class SearchDefinition(todo: Nothing, todo1: Nothing, columns: Seq[ResultColumn])

object SearchDefinition {
   def parseXML(todo: String): SearchDefinition = ???
}
