package com.radeusgd.archivum.datamodel

object DMUtils {
   def parsePath(path: String): List[String] = path.split('.').toList

   def makeGetter(path: List[String]): (DMValue) => DMValue =
      path match {
         case Nil => identity
         case part :: rest =>
            val nestedGetter: (DMValue) => DMValue = makeGetter(rest)
            (v: DMValue) =>
               nestedGetter(v.asInstanceOf[DMAggregate](part))
      }

   // for now setters only work for structs, arrays may be added later
   def makeSetter(path: List[String]): (DMStruct, DMValue) => DMStruct =
      path match {
         case Nil => throw new IllegalArgumentException
         case last :: Nil => _.updated(last, _)
         case part :: rest =>
            val nestedSetter: (DMStruct, DMValue) => DMStruct = makeSetter(rest)
            (s: DMStruct, v: DMValue) =>
               s.updated(part, nestedSetter(s(part).asInstanceOf[DMStruct], v))
      }

   def makeValueSetter(path: List[String]): (DMValue, DMValue) => DMValue =
      if (path.isEmpty) (_, n) => n
      else (o, v) => makeSetter(path)(o.asInstanceOf[DMStruct], v)
}
