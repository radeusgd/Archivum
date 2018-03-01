package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.persistence.DBTypes.DBType

trait Fetch {
   def getField(path: Seq[String], typ: DBType): DMValue

   def getSubTable(path: Seq[String]): Seq[Fetch]
}