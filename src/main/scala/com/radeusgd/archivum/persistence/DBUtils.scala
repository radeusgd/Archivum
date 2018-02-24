package com.radeusgd.archivum.persistence

object DBUtils {
   def pathToDb(path: Seq[String]): String = {
      if (path == Nil)
         "_"
      else
         path.mkString("_")
   }

   def dbToPath(dbPath: String): Seq[String] = dbPath.split('_')
}
