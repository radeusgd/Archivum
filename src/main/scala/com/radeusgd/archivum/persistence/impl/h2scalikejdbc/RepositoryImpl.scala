package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.datamodel.{DMStruct, Model}
import com.radeusgd.archivum.persistence.{DBUtils, Repository, RidSetHelper, SearchCriteria}
import scalikejdbc._

import com.radeusgd.archivum.utils.splitList

class RepositoryImpl(private val _model: Model,
                     private val tableName: String,
                     private val db: DB) extends Repository {

   private def rootType = model.roottype

   private val table: SQLSyntax = DBUtils.rawSql(tableName)

   override def createRecord(value: DMStruct): Rid = {
      val errors = rootType.validate(value)
      if (errors.nonEmpty) {
         val errMsgs = errors.map(err => err.getPath.mkString(".") + ": " + err.getMessage)
         throw new IllegalArgumentException("Value does not conform to the model\n" + errMsgs.mkString("\n"))
      }
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, value)
      db.autoCommit({ implicit session => ins.insert(None) })
   }

   private def rsToDM(rs: WrappedResultSet)(implicit session: DBSession): DMStruct = {
      val f = new FetchImpl(rs, tableName)
      rootType.tableFetch(Nil, f)
   }

   override def fetchRecord(rid: Rid): Option[DMStruct] = {
      val record = db.readOnly({ implicit session =>
         sql"SELECT * FROM $table WHERE _rid = $rid"
            .map(rs => rsToDM(rs)(session)).single.apply()
      })
      assert(record.forall(rootType.validate(_).isEmpty))
      record
   }

   override def updateRecord(rid: Rid, newValue: DMStruct): Unit = {
      if (rootType.validate(newValue).nonEmpty) throw new IllegalArgumentException("Value does not conform to the model")
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

   override def fetchAutocompletions(path: Seq[String], hint: String, limit: Int): Seq[String] = {
      val paths = splitList(path.toList, "*").reverse
      val lastPath = paths.head
      val subtables = paths.tail.reverse

      val actualTableName = subtables.foldLeft(tableName)(
         (tableName, part) => DBUtils.subtableName(tableName, part)
      )
      val actualTable = DBUtils.rawSql(actualTableName)
      val columnName = DBUtils.rawSql(DBUtils.pathToDb(lastPath))

      db.readOnly({ implicit session =>
         sql"SELECT DISTINCT $columnName FROM $actualTable WHERE $columnName LIKE ${hint + "%"} LIMIT $limit;"
            .map(rs => rs.string(1)).list.apply()
      })
   }

   override def fetchAllRecords(): Seq[(Rid, DMStruct)] = {
      val records = db.readOnly({ implicit session =>
         sql"SELECT * FROM $table"
            .map(rs => (rs.long("_rid"), rsToDM(rs)(session))).list.apply()
      })
      assert(records.forall(r => rootType.validate(r._2).isEmpty))
      records
   }

   private lazy val ridSetHelper = new RidSetHelperImpl(db, table)

   override def ridSet: RidSetHelper = ridSetHelper

   override def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = ???

   override def model: Model = _model
}
