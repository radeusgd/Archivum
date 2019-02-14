package com.radeusgd.archivum.querying.builtinqueries

import java.time.temporal.ChronoUnit

import cats.implicits._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.utils.Pipe._

class Chrzty(years: Int = 5) extends BuiltinQuery(years, Seq("Parafia", "Miejscowość")) {
   override def toString: String = "Chrzty"

   private def podsumujPłcie: ResultSet => Seq[ResultRow] =
      rs => rs.countWithPercentages(GroupByWithSummary("Płeć"))

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

   private def liczbaImion(rs: ResultSet): Seq[ResultRow] =
      rs.groupByHorizontal(GroupByWithSummary("Płeć"))
      .groupByHorizontal(GroupBy("Imiona.length"))
      .countAfterGrouping()

   private def pierwszeImionaPion(płeć: String)(rs: ResultSet): Seq[ResultRow] =
      rs
         .filter(path"Płeć"(_) == DMString(płeć))
         .filter(path"Imiona.length"(_).asInstanceOf[DMInteger].value > 0)
         .groupBy(GroupBy("Imiona.0",
            appendColumnMode = CustomAppendColumn("Imię"),
            sortType = PopularitySorted(Descending)))
         .aggregateClassic("Liczba" -> ClassicAggregations.count)

   private def pierwszeImiona(płeć: String)(rs: ResultSet): Seq[ResultRow] =
      rs
         .filter(path"Płeć"(_) == DMString(płeć))
         .filter(path"Imiona.length"(_).asInstanceOf[DMInteger].value > 0)
         .groupByHorizontal(GroupBy("Imiona.0", appendColumnMode = CustomAppendColumn("Imię")))
         .countAfterGrouping()

   private def grupujPoRodzajachUrodzeń: ResultSet => ResultSet =
      (rs: ResultSet) =>
      rs.groupByHorizontal(GroupByPredicates(
         "ogółem" -> (_ => true),
         "ślubne" ->
            (d => path"Urodzenie ślubne"(d).taknieasBool.contains(true)),
         "nieślubne" ->
            (d => path"Urodzenie ślubne"(d).taknieasBool.contains(false)),
         "bliźnięta" ->
            (d => bliźniętaEnum.contains(path"Ciąża mnoga"(d))),
         "urodzenia wielorakie" ->
            (d => urodzeniaWielokrotneEnum.contains(path"Ciąża mnoga"(d))),
         "żywe" ->
            (d => path"Urodzenie żywe"(d).taknieasBool.contains(true)),
         "martwe" ->
            (d => path"Urodzenie żywe"(d).taknieasBool.contains(false))
      ))

   private def grupujPoOsobie(path: String): ResultSet => ResultSet =
      (rs: ResultSet) => rs.groupBy(ComputedGroupBy(
         getter = (d: DMValue) => (for {
            imię <- path"$path.Imię" (d).asString
            nazwisko <- path"$path.Nazwisko" (d).asString
         } yield DMString(imię + " " + nazwisko)).getOrElse(DMNull),
         orderMapping = _.asString.getOrElse(""),
         appendColumnMode = CustomAppendColumn(path)
      ))

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
      /*"urodzenia_rocznie" -> Query(DataUrodzenia, podsumujPłcie),
      "urodzenia_miesięcznie" -> Query(DataUrodzenia,
         rs => rs.groupByHorizontal(groupByMonth("Data urodzenia")) |> podsumujPłcie),
      "dni_od_urodzenia_do_chrztu" -> Query(DataChrztu, liczbaDniOdUrodzeniaDoChrztu),
      "liczba_imion" -> Query(DataUrodzenia, liczbaImion),
      "pierwsze_imiona_pionowo_M" -> Query(DataUrodzenia, pierwszeImionaPion("M")),
      "pierwsze_imiona_pionowo_K" -> Query(DataUrodzenia, pierwszeImionaPion("K")),
      "pierwsze_imiona_M" -> Query(DataUrodzenia, pierwszeImiona("M")),
      "pierwsze_imiona_K" -> Query(DataUrodzenia, pierwszeImiona("K"))*/
      "księża" -> Query(DataChrztu, grupujPoOsobie("Udzielający chrztu") |> grupujPoRodzajachUrodzeń |> podsumujPłcie),
      "akuszerki" -> Query(DataChrztu, grupujPoOsobie("Akuszerka") |> grupujPoRodzajachUrodzeń |> podsumujPłcie)
   ))

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
