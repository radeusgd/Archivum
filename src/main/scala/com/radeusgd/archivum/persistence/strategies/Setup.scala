package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.persistence.DBTypes.DBType

trait Setup {
   def addField(path: Seq[String], typ: DBType): Unit

   def addSubTable(path: Seq[String]): Setup
}
