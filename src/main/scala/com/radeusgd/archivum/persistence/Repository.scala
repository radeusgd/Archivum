package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel.{DMStruct, Model}

trait Repository {
   type Rid = Int

   def model: Model

   def createRecord(value: DMStruct): Rid
   def fetchRecord(rid: Rid): Option[DMStruct]
   def updateRecord(rid: Rid, newValue: DMStruct): Unit
   def deleteRecord(rid: Rid): Unit

   // TODO using this in processing will be slow, in the future we should extend Repository to handle Streams of records or something similar
   def fetchAllRecords(): Seq[(Rid, DMStruct)]

   def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)]
}

// TODO if there are multiple backends this will diverge into various implementations
class RepositoryImpl extends Repository {
   override def model: Model = ???

   override def createRecord(value: DMStruct): Rid = ???

   override def fetchRecord(rid: Rid): Option[DMStruct] = ???

   override def updateRecord(rid: Rid, newValue: DMStruct): Unit = ???

   override def deleteRecord(rid: Rid): Unit = ???

   override def fetchAllRecords(): Seq[(Rid, DMStruct)] = ???

   override def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = ???
}
