package com.radeusgd.archivum.querying.builtinqueries

import com.radeusgd.archivum.datamodel.{DMInteger, DMValue}
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.querying.CustomGroupBy._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.utils.Pipe._

class Chrzty(years: Int = 1) extends BuiltinQuery(years, Seq("Parafia", "Miejscowość")) {
   override def toString: String = "Chrzty"

   private def podsumujPłcie(rs: ResultSet): Seq[ResultRow] =
      rs.groupByHorizontal(GroupBy("Płeć")).aggregate(
         (objs: Seq[DMValue]) => ResultRow(
            "Liczba" -> ClassicAggregations.count(objs),
            "%" -> 0
         )
      )
      /*rs.countTransposed("Płeć", Seq(
         "Chłopcy" -> "M",
         "Dziewczynki" -> "K"
      ), Some("brak danych"))*/

   private def podsumujCzySlubne(rs: ResultSet): Seq[ResultRow] =
      rs.aggregate(
         "Wszystkie" -> ClassicAggregations.count,
         /*Aggregations.countTransposed("Urodzenie ślubne", Seq(
            "Slubne" -> "Tak",
            "Nieślubne" -> "Nie"
         ), None):_**/
      )

   override val queries: Map[String, Query] = Map(
      "test_nazwiska" -> Query(DataUrodzenia, _.countGroups("Nazwisko", "Liczba osób")),
      "urodzenia_rocznie" -> Query(DataUrodzenia, podsumujPłcie),
      "urodzenia_miesięcznie" -> Query(DataUrodzenia,
         rs => rs.groupBy(groupByMonth("Data urodzenia")) |> podsumujPłcie),
      "ślubne_rocznie" -> Query(DataUrodzenia, podsumujCzySlubne),
   )
}
