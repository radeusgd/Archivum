package com.radeusgd.archivum

import com.radeusgd.archivum.datamodel.{DMNull, DMStruct, DMValue}

package object querying {
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
}
