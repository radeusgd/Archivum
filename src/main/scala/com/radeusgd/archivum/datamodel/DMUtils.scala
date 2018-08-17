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

   def makeSetter(path: List[String]): (DMValue, DMValue) => DMValue =
      path match {
         case Nil => throw new IllegalArgumentException
         case last :: Nil => _.asInstanceOf[DMAggregate].updated(last, _) // TODO this might be done better?
         case part :: rest =>
            val nestedSetter: (DMValue, DMValue) => DMValue = makeSetter(rest)
            (s: DMValue, v: DMValue) => {
               val agg = s.asInstanceOf[DMAggregate]
               agg.updated(part, nestedSetter(agg(part), v))
            }
      }

   def makeValueSetter(path: List[String]): (DMValue, DMValue) => DMValue =
      if (path.isEmpty) (_, n) => n
      else (o, v) => makeSetter(path)(o.asInstanceOf[DMStruct], v)
}