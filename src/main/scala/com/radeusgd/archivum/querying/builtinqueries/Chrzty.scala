package com.radeusgd.archivum.querying.builtinqueries

import com.radeusgd.archivum.querying._

class Chrzty(years: Int = 1) extends BuiltinQuery(years, Seq("Parafia", "Miejscowość")) {
   override def toString: String = "Chrzty"

   override val queries: Map[String, Query] = Map(
      "test_nazwiska" -> Query(DataUrodzenia, testQ)
   )

   def testQ(all: ResultSet): Seq[ResultRow] = {
      val grouped = all.groupBy(
         GroupBy("Nazwisko", PopularitySorted(Descending))
      )

      grouped.aggregate(
         "Liczba osób" -> Aggregations.count
      )
   }
}
