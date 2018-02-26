package com.radeusgd.archivum.persistence.strategies

import scalikejdbc._

import scala.collection.mutable

trait Insert {
   def setValue(path: Seq[String], value: Any): Unit // TODO
   def setSubTable(path: Seq[String], amount: Int): Seq[Insert]
}

import com.radeusgd.archivum.persistence.DBUtils._

class InsertImpl(val tableName: String) extends Insert {
   var children: List[InsertImpl] = Nil
   val values: mutable.Map[String, Any] = mutable.Map.empty

   override def setValue(path: Seq[String], value: Any): Unit =
      values.put(pathToDb(path), value)

   override def setSubTable(path: Seq[String], amount: Int): Seq[Insert] = {
      val c = (1 to amount).map(_ => new InsertImpl(subtableName(tableName, path)))
      children ++= c
      c
   }

   private def makeInsert(pairs: List[(String, Any)]): SQL[Nothing, NoExtractor] = {
      val names = join(pairs.map({ case (name, _) => rawSql(name)}), sqls",")
      val vals = join(pairs.map({ case (_, v) => sqls"$v"}), sqls",")

      sql"INSERT INTO ${rawSql(tableName)} ($names) VALUES($vals);"
   }

   def insert(rid: Option[Rid], prid: Option[Rid] = None)(implicit session: DBSession): Long = {
      val vp: List[(String, Any)] = values.toList ++ prid.map(id => ("_prid", id)).toList
      val key = rid match {
         case Some(id) =>
            makeInsert(vp ++ List(("_rid", id))).update.apply()
            id
         case None =>
            makeInsert(vp).updateAndReturnGeneratedKey().apply()
      }

      children.foreach(c => c.insert(None, Some(key))(session))
      key
   }
}