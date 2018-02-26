package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel.{DMStruct, Model}
import com.radeusgd.archivum.persistence.strategies.InsertImpl
import scalikejdbc._

trait Repository {
   type Rid = Long

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
class RepositoryImpl(
                       private val _model: Model,
                       private val tableName: String,
                       private val db: DB) extends Repository {

   private def rootType = model.roottype

   override def createRecord(value: DMStruct): Rid = {
      assert(rootType.validate(value).isEmpty)
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, value)
      db.autoCommit({ implicit session => ins.insert(None) })
   }

   override def fetchRecord(rid: Rid): Option[DMStruct] = ???

   override def updateRecord(rid: Rid, newValue: DMStruct): Unit = {
      assert(rootType.validate(newValue).isEmpty)
      deleteRecord(rid)
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, newValue)
      val nrid = db.autoCommit({ implicit session => ins.insert(Some(rid)) })
      assert (rid == nrid)
   }

   override def deleteRecord(rid: Rid): Unit = {
      db.autoCommit({ implicit session =>
         sql"DELETE FROM ${DBUtils.rawSql(tableName)} WHERE _rid = $rid;".update.apply()
      })
   }

   override def fetchAllRecords(): Seq[(Rid, DMStruct)] = ???

   override def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = ???

   override def model: Model = _model
}
