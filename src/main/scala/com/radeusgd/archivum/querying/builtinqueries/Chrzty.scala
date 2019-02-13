package com.radeusgd.archivum.querying.builtinqueries

import java.time.temporal.ChronoUnit

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

   private def hasDate(path: String)(dMValue: DMValue): Boolean =
      DMUtils.makeGetter(path)(dMValue).asType[DMYearDate].exists(_.fullDate.isDefined)

   private def liczbaDniOdUrodzeniaDoChrztu(rs: ResultSet): Seq[ResultRow] = {
      rs.filter(hasDate("Data urodzenia") _ && hasDate("Data chrztu")).countWithPercentages(ComputedGroupBy(
         getter = (dmv: DMValue) => {
            val diff: Option[Int] = for {
               bdatefield <- path"Data urodzenia"(dmv).asType[DMYearDate]
               bdate <- bdatefield.fullDate
               cdatefield <- path"Data chrztu"(dmv).asType[DMYearDate]
               cdate <- cdatefield.fullDate
            } yield ChronoUnit.DAYS.between(bdate, cdate).toInt

            val d = diff.get // we know this will exist because of earlier filter
            if (d < 0) DMString("błąd")
            else if (d >= 15) DMString("15>")
            else DMInteger(d)
         },
         orderMapping = {
            case DMInteger(v) => v
            case DMString(s) =>
               if (s == "15>") 15
               else -1
         }
      ))
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
            /*"ślubne/" + name ->
               query.withFilter(d => path"Urodzenie ślubne"(d).taknieasBool.contains(true)),
            "nieślubne/" + name ->
               query.withFilter(d => path"Urodzenie ślubne"(d).taknieasBool.contains(false)),
            "martwe/" + name ->
               query.withFilter(d => path"Urodzenie żywe"(d).taknieasBool.contains(false)),
            "urodzenia_pojedyncze/" + name ->
               query.withFilter(d => path"Ciąża mnoga"(d) == DMString("NIE")),
            "urodzenia_bliźniacze/" + name ->
               query.withFilter(d => bliźniętaEnum.contains(path"Ciąża mnoga"(d))),
            //FIXME temporarily disable for faster testing*/"urodzenia_wielorakie/" + name ->
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
      "dni_od_urodzenia_do_chrztu" -> Query(DataChrztu, liczbaDniOdUrodzeniaDoChrztu),
      "dni_debug" -> Query(DataChrztu, rs => rs.filter(hasDate("Data urodzenia") _ && hasDate("Data chrztu")).aggregateClassic("l" -> ClassicAggregations.count))
   ))

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
