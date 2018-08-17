package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.persistence.DBUtils.{pathToDb, rawSql, subtableName}
import com.radeusgd.archivum.persistence.strategies.Fetch
import scalikejdbc._

class FetchImpl(private val rs: WrappedResultSet, private val tableName: String)
               (private implicit val session: DBSession) extends Fetch {

   override def getString(path: Seq[String]): String = rs.string(pathToDb(path))

   override def getInt(path: Seq[String]): Option[Int] = rs.intOpt(pathToDb(path))

   override def getSubTable[T](path: Seq[String], subMapper: Fetch => T): Seq[T] = {
      val subname = subtableName(tableName, path)
      val subtable = rawSql(subname)
      val prid = rs.long("_rid")
      sql"SELECT * FROM $subtable WHERE _prid = $prid".map(rs => subMapper(new FetchImpl(rs, subname))).list.apply()
   }
}
