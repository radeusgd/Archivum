package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.datamodel.{DMStruct, Model}
import com.radeusgd.archivum.persistence.{DBUtils, Repository, RidSetHelper, SearchCriteria}
import scalikejdbc._

class RepositoryImpl(private val _model: Model,
                     private val tableName: String,
                     private val db: DB) extends Repository {

   private def rootType = model.roottype

   private val table: SQLSyntax = DBUtils.rawSql(tableName)

   override def createRecord(value: DMStruct): Rid = {
      assert(rootType.validate(value).isEmpty)
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, value)
      db.autoCommit({ implicit session => ins.insert(None) })
   }

   private def rsToDM(rs: WrappedResultSet)(implicit session: DBSession): DMStruct = {
      val f = new FetchImpl(rs, tableName)
      rootType.tableFetch(Nil, f)
   }

   override def fetchRecord(rid: Rid): Option[DMStruct] =
      db.readOnly({ implicit session =>
         sql"SELECT * FROM $table WHERE _rid = $rid"
            .map(rs => rsToDM(rs)(session)).single.apply()
      })

   override def updateRecord(rid: Rid, newValue: DMStruct): Unit = {
      assert(rootType.validate(newValue).isEmpty)
      deleteRecord(rid)
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, newValue)
      val nrid = db.autoCommit({ implicit session => ins.insert(Some(rid)) })
      assert(rid == nrid)
   }

   override def deleteRecord(rid: Rid): Unit = {
      db.autoCommit({ implicit session =>
         sql"DELETE FROM $table WHERE _rid = $rid;".update.apply()
      })
   }

   override def fetchAllRecords(): Seq[(Rid, DMStruct)] = {
      db.readOnly({ implicit session =>
         sql"SELECT * FROM $table"
            .map(rs => (rs.long("_rid"), rsToDM(rs)(session))).list.apply()
      })
   }

   private lazy val ridSetHelper = new RidSetHelperImpl(db, table)

   override def ridSet: RidSetHelper = ridSetHelper

   override def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = ???

   override def model: Model = _model
}