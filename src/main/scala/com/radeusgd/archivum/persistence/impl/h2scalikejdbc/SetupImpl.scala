package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.persistence.DBTypes
import com.radeusgd.archivum.persistence.DBTypes.DBType
import com.radeusgd.archivum.persistence.DBUtils.{join, pathToDb, rawSql, subtableName}
import com.radeusgd.archivum.persistence.strategies.Setup
import scalikejdbc._

import scala.collection.mutable

class SetupImpl(val tableName: String, val subOf: Option[String] = None) extends Setup {
   private val fields: mutable.Map[String, DBType] = mutable.Map.empty
   private val tables: mutable.Map[String, SetupImpl] = mutable.Map.empty
   private val sqlTableName: SQLSyntax = rawSql(tableName)

   override def addField(path: Seq[String], typ: DBType): Unit = {
      fields.put(pathToDb(path), typ)
   }

   override def addSubTable(path: Seq[String]): Setup = {
      val sub = new SetupImpl(subtableName(tableName, path), Some(tableName))
      tables.put(pathToDb(path), sub)
      sub
   }

   private def defineColumn(name: String, dbtype: DBType): SQLSyntax = {
      val typename: String = dbtype match {
         case DBTypes.Integer => "INT"
         case DBTypes.String => "VARCHAR(9000)"
         case DBTypes.Date => "VARCHAR(10)" // TODO decide on dates, but probably string storage will be better to make sure we support historic dates correctly
      }
      rawSql(s"$name $typename")
   }

   def createSchema(): List[SQL[Nothing, NoExtractor]] = {
      val rid = rawSql("_rid BIGINT AUTO_INCREMENT PRIMARY KEY")
      val prid = subOf.map(parent => rawSql("_prid BIGINT AUTO_INCREMENT REFERENCES " + parent + " ON DELETE CASCADE"))
      val columns: SQLSyntax = join(List(rid) ++ prid.toList ++ fields.map({ case (name, typ) => defineColumn(name, typ) }), sqls",")
      val schem = sql"CREATE TABLE $sqlTableName ($columns);"
      List(schem) ++ tables.values.toList.flatMap(_.createSchema())
   }
}
