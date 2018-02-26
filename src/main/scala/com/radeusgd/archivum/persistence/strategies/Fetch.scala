package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.datamodel.{DMInteger, DMString, DMStruct, DMValue}
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.DBTypes.{DBType, Date, Integer}
import scalikejdbc.{DBSession, WrappedResultSet}

trait Fetch {
   def getField(path: Seq[String], typ: DBType): DMValue

   def getSubTable(path: Seq[String]): Seq[Fetch]
}

import scalikejdbc._
import com.radeusgd.archivum.persistence.DBUtils._

class FetchImpl(private val rs: WrappedResultSet, private val tableName: String)
               (private implicit val session: DBSession) extends Fetch {

   override def getField(path: Seq[String], typ: DBType): DMValue = {
      val name = pathToDb(path)
      typ match {
         case DBTypes.Integer => DMInteger(rs.int(name))
         case DBTypes.String => DMString(rs.string(name))
         case DBTypes.Date => ???
      }
   }

   override def getSubTable(path: Seq[String]): Seq[Fetch] = {
      val subname = subtableName(tableName, path)
      val subtable = rawSql(subname)
      val prid = rs.long("_rid")
      sql"SELECT * FROM $subtable WHERE _prid = $prid".map(rs => new FetchImpl(rs, subname)).list.apply()
   }
}