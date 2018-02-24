package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.persistence.DBTypes.DBType

trait Fetch {
   def getField(path: Seq[String], typ: DBType): DMValue
   def getSubTable(path: Seq[String]): Seq[Fetch]
}

class FetchImpl extends Fetch {
   // TODO
   override def getField(path: Seq[String], typ: DBType): DMValue = ???

   override def getSubTable(path: Seq[String]): Seq[Fetch] = ???
}