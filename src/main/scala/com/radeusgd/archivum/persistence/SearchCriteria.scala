package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel._

sealed abstract class SearchCriteria {
   def check(v: DMValue): Boolean
}

// warning - for now searching over array's content isn't supported (but TODO add it)
case class Equal(path: Seq[String], value: DMValue) extends SearchCriteria {
   override def check(v: DMValue): Boolean =
      DMUtils.makeGetter(path.toList)(v) == value
}

case class HasPrefix(path: Seq[String], prefix: String) extends SearchCriteria {
   override def check(v: DMValue): Boolean =
      DMUtils.makeGetter(path.toList)(v) match {
         case DMString(value) => value.startsWith(prefix)
         case _ => false
      }
}

case class And(criteria: SearchCriteria*) extends SearchCriteria {
   override def check(v: DMValue): Boolean =
      criteria.forall(_.check(v))
}

case class Or(criteria: SearchCriteria*) extends SearchCriteria {
   override def check(v: DMValue): Boolean =
      criteria.exists(_.check(v))
}

object Truth extends SearchCriteria {
   override def check(v: DMValue): Boolean = true
}