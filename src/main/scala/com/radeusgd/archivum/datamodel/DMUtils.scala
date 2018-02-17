package com.radeusgd.archivum.datamodel

object DMUtils {
   def parsePath(path: String): List[String] = path.split('.').toList

   // TODO not sure if take DMValue or DMStruct
   def makeGetter(path: List[String]): (DMAggregate) => DMValue =
      path match {
         case Nil => throw new IllegalArgumentException // TODO NonEmptySeq
         case last :: Nil => _.apply(last)
         case part :: rest =>
            val nestedGetter: (DMAggregate) => DMValue = makeGetter(rest);
         { agg: DMAggregate =>
            nestedGetter(agg(part).asInstanceOf[DMAggregate])
         }
      }

   // for now setters only work for structs, arrays may be added later
   def makeSetter(path: List[String]): (DMStruct, DMValue) => DMStruct =
      path match {
         case Nil => throw new IllegalArgumentException // TODO NonEmptySeq
         case last :: Nil => _.updated(last, _)
         case part :: rest =>
            val nestedSetter: (DMStruct, DMValue) => DMStruct = makeSetter(rest);
         { (s: DMStruct, v: DMValue) =>
            s.updated(part, nestedSetter(s(part).asInstanceOf[DMStruct], v))
         }
      }
}
