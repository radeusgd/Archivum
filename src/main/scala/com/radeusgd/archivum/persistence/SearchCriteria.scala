package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel.DMValue

sealed abstract class SearchCriteria

// warning - for now searching over array's content isn't supported (but TODO add it)
case class Equal(path: Seq[String], value: DMValue)

case class HasPrefix(path: Seq[String], prefix: String)

case class And(criteria: Seq[SearchCriteria])

case class Or(criteria: Seq[SearchCriteria])