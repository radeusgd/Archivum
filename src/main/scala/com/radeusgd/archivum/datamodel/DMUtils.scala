package com.radeusgd.archivum.datamodel

object DMUtils {
   def parsePath(path: String): List[String] = path.split('.').toList

   def makeGetter(path: List[String]): (DMValue) => DMValue =
      path match {
         case Nil => identity
         case part :: rest =>
            val nestedGetter: (DMValue) => DMValue = makeGetter(rest)
            (v: DMValue) => {
               //println(part + " of " + v) // TODO remove me (debug)
               nestedGetter(v.asInstanceOf[DMAggregate](part))
            }
      }

   def makeSetter(path: List[String]): (DMAggregate, DMValue) => DMAggregate =
      path match {
         case Nil => throw new IllegalArgumentException
         case last :: Nil => _.updated(last, _)
         case part :: rest =>
            val nestedSetter: (DMAggregate, DMValue) => DMAggregate = makeSetter(rest)
            (s: DMAggregate, v: DMValue) =>
               s.updated(part, nestedSetter(s(part).asInstanceOf[DMAggregate], v))
      }

   def makeValueSetter(path: List[String]): (DMValue, DMValue) => DMValue =
      if (path.isEmpty) (_, n) => n
      else (o, v) => makeSetter(path)(o.asInstanceOf[DMStruct], v)
}
