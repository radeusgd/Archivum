package com.radeusgd.archivum

import com.radeusgd.archivum.datamodel.{DMNull, DMStruct, DMUtils, DMValue}

package object querying {

   @deprecated
   def extractKey(key: String, safe: Boolean = true): (DMValue => DMValue) = {
      (v: DMValue) => {
         try {
            val struct: DMStruct = v.asInstanceOf[DMStruct]
            struct.values.getOrElse(key, if (safe) DMNull else throw new NoSuchElementException(key))
         } catch {
            case e: ClassCastException => if (safe) DMNull else throw e
         }
      }
   }

   implicit class GetterHelper(val sc: StringContext) extends AnyVal {
      def path(args: Any*): DMValue => DMValue =
         DMUtils.makeGetter(sc.s(args))

   }

   type ResultRow = NestedMap[String, DMValue]
}
