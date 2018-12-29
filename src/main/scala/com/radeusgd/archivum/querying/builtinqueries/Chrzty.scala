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
      val grouped = repo.fetchAllGrouped(
         Grouping("Miejscowość", PopularitySorted(Descending))
      )
      grouped.aggregate(
         "Liczba" -> Aggregations.count
      )
   }
}
