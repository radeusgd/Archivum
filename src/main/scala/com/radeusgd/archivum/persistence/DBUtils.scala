package com.radeusgd.archivum.persistence

import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax

object DBUtils {
   type Rid = Long

   def sanitizeName(name: String): String =
      name.replace(' ', '_')

   private val dbSep: String = "__"

   def pathToDb(path: Seq[String]): String = {
      if (path == Nil) dbSep
      else path.map(sanitizeName).mkString(dbSep)
   }

   //def dbToPath(dbPath: String): Seq[String] = dbPath.split("__")

   def join(parts: Iterable[SQLSyntax], sep: SQLSyntax): SQLSyntax =
      if (parts.isEmpty) sqls""
      else parts.tail.foldLeft(parts.head)(_ + sep + _)

   def rawSql(str: String): SQLSyntax = SQLSyntax.createUnsafely(str)

   def subtableName(parent: String, path: Seq[String]): String = parent + dbSep + pathToDb(path)
}
