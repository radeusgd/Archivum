package com.radeusgd.archivum.persistence

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc._

object DBUtils {
   def sanitizeName(name: String): String =
      name.replace(' ', '_')

   def pathToDb(path: Seq[String]): String = {
      if (path == Nil)
         "_"
      else
         path.map(sanitizeName).mkString("__")
   }

   def dbToPath(dbPath: String): Seq[String] = dbPath.split("__")

   def join(parts: Iterable[SQLSyntax], sep: SQLSyntax): SQLSyntax =
      if (parts.isEmpty) sqls""
      else parts.tail.foldLeft(parts.head)(_ + sep + _)

   def rawSql(str: String): SQLSyntax = SQLSyntax.createUnsafely(str)
}
