package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel._

sealed abstract class SearchCriteria

// warning - for now searching over array's content isn't supported (but TODO add it)
case class Equal(path: Seq[String], value: DMValue) extends SearchCriteria
case class HasPrefix(path: Seq[String], prefix: String) extends SearchCriteria
case class And(criteria: SearchCriteria*) extends SearchCriteria
case class Or(criteria: SearchCriteria*) extends SearchCriteria
object Truth extends SearchCriteria

object SearchCriteria {
   def makePredicate(sc: SearchCriteria)(v: DMValue): Boolean = sc match {
      case Equal(path, value) =>
         DMUtils.makeGetter(path.toList)(v) == value
      case HasPrefix(path, prefix) =>
         DMUtils.makeGetter(path.toList)(v) match {
            case DMString(value) => value.startsWith(prefix)
            case _ => false
         }
      case And(criteria @_*) => criteria.forall(makePredicate(_)(v))
      case Or(criteria @_*) => criteria.exists(makePredicate(_)(v))
      case Truth => true
   }
}