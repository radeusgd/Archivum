package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.datamodel.{DMStruct, DMUtils, DMValue, Model}
import com.radeusgd.archivum.persistence._
import scalikejdbc._
import com.radeusgd.archivum.utils.splitList
import com.radeusgd.archivum.persistence.DBUtils.{pathToDb, rawSql}
import com.radeusgd.archivum.persistence.strategies.Insert

trait ReadOnlySessionProvider {
   def readOnlySession[T](execution: DBSession => T): T
}

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
      localTx({ implicit session => ins.insert(None) })
   }

   private def rsToDM(rs: WrappedResultSet): DMStruct = {
      val f = new FetchImpl(rs, tableName)(makeRSP)
      rootType.tableFetch(Nil, f)
   }

   override def fetchRecord(rid: Rid): Option[DMStruct] = {
      val record = readOnly({ implicit session =>
         sql"SELECT * FROM $table WHERE _rid = $rid"
            .map(rs => rsToDM(rs)).single.apply()
      })
      assert(record.forall(rootType.validate(_).isEmpty))
      record
   }

   override def updateRecord(rid: Rid, newValue: DMStruct): Unit = {
      if (rootType.validate(newValue).nonEmpty) throw new IllegalArgumentException("Value does not conform to the model")
      deleteRecord(rid)
      val ins = new InsertImpl(tableName)
      rootType.tableInsert(Nil, ins, newValue)
      val nrid = localTx({ implicit session => ins.insert(Some(rid)) })
      assert(rid == nrid)
   }

   override def deleteRecord(rid: Rid): Unit = {
      localTx({ implicit session =>
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
      val actualTable = rawSql(actualTableName)
      val columnName = rawSql(pathToDb(lastPath))

      readOnly({ implicit session =>
         sql"SELECT DISTINCT $columnName FROM $actualTable WHERE $columnName LIKE ${hint + "%"} LIMIT $limit;"
            .map(rs => rs.string(1)).list.apply()
      })
   }

   override def fetchAllRecords(): Seq[(Rid, DMStruct)] = suppressLogging {
      val records = readOnly({ implicit session =>
         sql"SELECT * FROM $table"
            .map(rs => (rs.long("_rid"), rsToDM(rs))).list.apply()
      })
      //assert(records.forall(r => rootType.validate(r._2).isEmpty))
      assert(records.headOption.forall(r => rootType.validate(r._2).isEmpty)) // check only first record
      records
   }

   // TODO make it optimized by using SELECT * ORDER BY (a,b,c) and than split groups in O(N)
   // override def fetchAllGrouped

   override def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = minimalLogging {
      val cond: SQLSyntax = makeCondition(criteria)
      val records = readOnly({ implicit session =>
         sql"SELECT * FROM $table WHERE $cond"
            .map(rs => (rs.long("_rid"), rsToDM(rs))).list.apply()
      })
      //assert(records.forall(r => rootType.validate(r._2).isEmpty))
      assert(records.headOption.forall(r => rootType.validate(r._2).isEmpty)) // check only first record
      records
   }

   override def searchIds(criteria: SearchCriteria): Seq[Rid] = {
      val cond: SQLSyntax = makeCondition(criteria)
      val rids = readOnly({ implicit session =>
         sql"SELECT * FROM $table WHERE $cond"
            .map(rs => rs.long("_rid")).list.apply()
      })
      rids
   }

   //override def fetchIds(ids: Seq[Rid]): Seq[(Rid, DMStruct)] = ??? // TODO can this be optimized?

   override def getAllDistinctValues(path: List[String], filter: SearchCriteria): List[DMValue] = {
      val table = rawSql(tableName)
      val columnName = rawSql(pathToDb(path))
      val cond = makeCondition(filter)

      val typ = rootType.getType(path)

      def rsToDV(rs: WrappedResultSet): DMValue = {
         val f = new FetchImpl(rs, null)(makeRSP) // TODO distinct values won't work with subtables
         typ.tableFetch(path, f)
      }

      readOnly({ implicit session =>
         sql"SELECT DISTINCT $columnName FROM $table WHERE $cond;"
            .map(rsToDV).list.apply()
      })
   }

   private def readOnly[T](execution: DBSession => T): T = synchronized {
      db.readOnly { execution }
   }

   def makeRSP: ReadOnlySessionProvider = new ReadOnlySessionProvider {
      override def readOnlySession[T](execution: DBSession => T): T =
         suppressLogging { // we don't want too much logging in nested queries
            readOnly(execution)
         }
   }

   private def localTx[T](execution: DBSession => T): T = synchronized {
      db.localTx { execution }
   }

   private lazy val ridSetHelper = new RidSetHelperImpl(db, table)

   override def ridSet: RidSetHelper = ridSetHelper

   override def model: Model = _model

   private def suppressLogging[T](action: => T): T = {
      val prevSettings = GlobalSettings.loggingSQLAndTime
      GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
         enabled = false,
         singleLineMode = false,
         printUnprocessedStackTrace = false,
         stackTraceDepth= 1,
         logLevel = 'debug,
         warningEnabled = false,
         warningThresholdMillis = 3000L,
         warningLogLevel = 'warn
      )
      val res: T = action
      GlobalSettings.loggingSQLAndTime = prevSettings
      res
   }

   private def minimalLogging[T](action: => T): T = {
      val prevSettings = GlobalSettings.loggingSQLAndTime
      GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
         enabled = true,
         singleLineMode = true,
         printUnprocessedStackTrace = false,
         stackTraceDepth= 0,
         logLevel = 'debug,
         warningEnabled = true,
         warningThresholdMillis = 3000L,
         warningLogLevel = 'warn
      )
      val res: T = action
      GlobalSettings.loggingSQLAndTime = prevSettings
      res
   }

   private def convertDMToDBForCondition(path: Seq[String], v: DMValue): Any = {
      class FakeInsert extends Insert {
         var innerValue: Any = null
         override def setValue(path: Seq[String], value: Any): Unit = innerValue = value
         override def setSubTable(path: Seq[String], amount: Int): Seq[Insert] = throw new RuntimeException("Filtering over table content not supported")
      }

      val fakeInsert = new FakeInsert
      rootType.getType(path.toList).tableInsert(Nil, fakeInsert, v)
      fakeInsert.innerValue
   }

   private def makeCondition(sc: SearchCriteria): SQLSyntax =
      sc match {
         case Equal(path, value) =>
            val fieldName = rawSql(pathToDb(path))
            val sqlvalue = convertDMToDBForCondition(path, value)
            sqls"$fieldName = $sqlvalue"
         case HasPrefix(path, prefix) =>
            val fieldName = rawSql(pathToDb(path))
            val pref = prefix + "%"
            sqls"$fieldName LIKE $pref" // FIXME THIS IS UNTESTED AND LIKELY MAY NOT WORK CORRECTLY
//         case Like(path, str) => // TODO this could use H2 full-text search capability
//            val fieldName = rawSql(pathToDb(path))
//            val exp = "%" + str + "%"
//            sqls"$fieldName LIKE $exp" // FIXME THIS IS UNTESTED AND LIKELY MAY NOT WORK CORRECTLY
         case And() => makeCondition(Truth)
         case And(cond) => makeCondition(cond)
         case And(c1, rest @_*) => sqls"(${makeCondition(c1)} AND ${makeCondition(And(rest:_*))})"
         case Or() => makeCondition(Truth)
         case Or(cond) => makeCondition(cond)
         case Or(c1, rest @_*) => sqls"(${makeCondition(c1)} OR ${makeCondition(Or(rest:_*))})"
         case Truth => sqls"(1 = 1)"
      }

}