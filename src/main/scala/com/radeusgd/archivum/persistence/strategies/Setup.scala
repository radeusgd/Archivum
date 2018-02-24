package com.radeusgd.archivum.persistence.strategies

import com.radeusgd.archivum.persistence.DBTypes.DBType
import com.radeusgd.archivum.persistence.DBUtils

import scala.collection.mutable

trait Setup {
   def addField(path: Seq[String], typ: DBType): Unit
   def addSubTable(path: Seq[String]): Setup
}

import scalikejdbc._

class SetupImpl extends Setup {
   val fields: mutable.Map[String, DBType] = mutable.Map.empty
   val tables: mutable.Map[String, SetupImpl] = mutable.Map.empty

   override def addField(path: Seq[String], typ: DBType): Unit = {
      fields.put(DBUtils.pathToDb(path), typ)
   }

   override def addSubTable(path: Seq[String]): Setup = {
      val sub = new SetupImpl
      tables.put(DBUtils.pathToDb(path), sub)
      sub
   }

   // TODO
   def createSchema() = ???
   def delete(rid: Long) = ???
}