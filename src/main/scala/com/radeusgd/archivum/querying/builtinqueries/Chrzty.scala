package com.radeusgd.archivum.querying.builtinqueries
import com.radeusgd.archivum.datamodel.DMStruct
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying._

class Chrzty extends BuiltinQuery {
   override def toString: String = "Chrzty"

   override val queries: Map[String,Repository => Seq[ResultRow]] = Map(
      "test" -> testQ
   )

   def testQ(repo: Repository): Seq[ResultRow] = {
      val values = repo.fetchAllRecords().map(_._2)
      val all = ResultSet(Seq(MultipleResultRow(values)))
      val grouped = all.groupBy("Miejscowość", Some(AppendPrefix("Miejscowość")), _.toString)

      grouped.aggregate(
         "Liczba" -> Aggregations.count
      )
   }
}
