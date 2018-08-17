package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.persistence.impl.h2scalikejdbc.DatabaseImpl
import scalikejdbc._

trait Database {
   def openRepository(modelName: String): Option[Repository]

   def createRepository(modelDefinition: String): Unit // TODO see Impl

   def listRepositories(): Seq[String]
}

object Database {
   def open(): Database = {
      ConnectionPool.singleton("jdbc:h2:file:./database", "", "")
      val db = DB(ConnectionPool.borrow())
      db.autoClose(false)
      new DatabaseImpl(db)
   }
}