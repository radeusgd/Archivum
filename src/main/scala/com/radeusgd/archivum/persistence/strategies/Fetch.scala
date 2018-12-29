package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes.DBType

trait Fetch {
   def getString(path: Seq[String]): String
   def getInt(path: Seq[String]): Option[Int] // ints are nullable

   def getSubTable[T](path: Seq[String], subMapper: Fetch => T): () => Seq[T]
}