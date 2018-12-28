package com.radeusgd.archivum.querying.builtinqueries
import com.radeusgd.archivum.datamodel.DMStruct
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.{AppendPrefix, MultipleResultRow, ResultRow, ResultSet}

class Chrzty extends BuiltinQuery {
   override def toString: String = "Chrzty"

   override val queries: Map[String,Repository => Seq[ResultRow]] = Map(
      "test" -> testQ
   )

   def testQ(repo: Repository): Seq[ResultRow] = {
      val all = ResultSet(Seq(MultipleResultRow(repo.fetchAllRecords().map(_._2))))
      val grouped = all.groupBy("Miejscowość", Some(AppendPrefix("Miejscowość")), _.toString)
      grouped.getResult
   }
}
