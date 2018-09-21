package com.radeusgd.archivum.languages.AST

sealed abstract class Query

case class BaseQuery() extends Query

case class Filter(base: Query, predicate: Expression)

case class UnpackArray(base: Query, path: String)

case class Flatten(base: Query)

//case class GroupBy(base: Query, path: String, columnName: String, ) // TODO sorting