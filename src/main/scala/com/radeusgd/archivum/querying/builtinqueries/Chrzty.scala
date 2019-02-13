package com.radeusgd.archivum.querying.builtinqueries

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.querying.CustomGroupBy._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.utils.Pipe._
import cats.implicits._

class Chrzty(years: Int = 5) extends BuiltinQuery(years, Seq("Parafia", "Miejscowość")) {
   override def toString: String = "Chrzty"

   private def podsumujPłcie(rs: ResultSet): Seq[ResultRow] =
      rs.countWithPercentages(GroupByWithSummary("Płeć"))

   private def podsumujCzySlubne(rs: ResultSet): Seq[ResultRow] =
      rs.aggregateClassic(
         Seq("Wszystkie" -> ClassicAggregations.count) ++
            ClassicAggregations.countTransposed("Urodzenie ślubne", Seq(
               "Slubne" -> "Tak",
               "Nieślubne" -> "Nie"
            ), None):_*
      )

   private def zakresDat(rs: ResultSet): Seq[ResultRow] = {
      def getYears(objs: Seq[DMValue]): Seq[Int] =
         objs.getProp("Data chrztu").onlyWithType[DMYearDate].map(_.year)

      rs.groupBy(GroupBy("Miejscowość")).aggregate((objs: Seq[DMValue]) => {
         val years = getYears(objs)

         val first: DMValue = if (years.isEmpty) DMNull else years.min
         val last: DMValue = if (years.isEmpty) DMNull else years.max
         ResultRow(
            "Pierwszy rok" -> first,
            "Ostatni rok" -> last
         )
      })
   }

   private val bliźniętaEnum: Seq[DMString] = Seq("M+M", "M+K", "K+K").map(DMString)
   private val urodzeniaWielokrotneEnum: Seq[DMString] = Seq("M+M+M",
                "M+M+K",
                "M+K+K",
                "K+K+K",
                "4",
                "5",
                "więcej").map(DMString)

   private def makeAlternativesForSpecialGroups(queries: (String, Query)*): Map[String, Query] =
      queries.foldLeft[Map[String, Query]](Map.empty){ case (map, (name, query)) =>
         map.merged(Map(
            name -> query,
            "ślubne/" + name ->
               query.withFilter(d => path"Urodzenie ślubne"(d).taknieasBool.contains(true)),
            "nieślubne/" + name ->
               query.withFilter(d => path"Urodzenie ślubne"(d).taknieasBool.contains(false)),
            "martwe/" + name ->
               query.withFilter(d => path"Urodzenie żywe"(d).taknieasBool.contains(false)),
            "urodzenia_pojedyncze/" + name ->
               query.withFilter(d => path"Ciąża mnoga"(d) == DMString("NIE")),
            "urodzenia_bliźniacze/" + name ->
               query.withFilter(d => bliźniętaEnum.contains(path"Ciąża mnoga"(d))),
            "urodzenia_wielorakie/" + name ->
               query.withFilter(d => urodzeniaWielokrotneEnum.contains(path"Ciąża mnoga"(d)))
         ))
      }


   override val groupedQueries: Map[String, Query] = Map(
      "test_nazwiska" -> Query(DataUrodzenia, _.countGroups("Nazwisko", "Liczba osób")),
      "ślubne_rocznie" -> Query(DataUrodzenia, podsumujCzySlubne),
   ).merged(makeAlternativesForSpecialGroups(
      "urodzenia_rocznie" -> Query(DataUrodzenia, podsumujPłcie),
      "urodzenia_miesięcznie" -> Query(DataUrodzenia,
         rs => rs.groupByHorizontal(groupByMonth("Data urodzenia")) |> podsumujPłcie),
   ))

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
