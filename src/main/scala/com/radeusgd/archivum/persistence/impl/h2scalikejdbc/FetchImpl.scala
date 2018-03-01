package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.datamodel.{DMInteger, DMNull, DMString, DMValue}
import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.DBTypes.DBType
import com.radeusgd.archivum.persistence.DBUtils.{pathToDb, rawSql, subtableName}
import com.radeusgd.archivum.persistence.strategies.Fetch
import scalikejdbc._

class FetchImpl(private val rs: WrappedResultSet, private val tableName: String)
               (private implicit val session: DBSession) extends Fetch {

   override def getField(path: Seq[String], typ: DBType): DMValue = {
      val name = pathToDb(path)
      typ match {
         case DBTypes.Integer => rs.intOpt(name).map(DMInteger).getOrElse(DMNull)
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
