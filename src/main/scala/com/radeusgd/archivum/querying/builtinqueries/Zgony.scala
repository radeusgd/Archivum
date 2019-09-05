package com.radeusgd.archivum.querying.builtinqueries

import java.time.temporal.ChronoUnit

import cats.implicits._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying.CustomGroupBy._
import com.radeusgd.archivum.querying._

class Zgony(years: Int, folderGroupings: Seq[String], charakter: Option[String] = None)
   extends BuiltinQuery(years, folderGroupings, charakter) {

   private def zakresDat(rs: ResultSet): Seq[ResultRow] = {
      def getYears(objs: Seq[DMValue]): Seq[Int] =
         objs.getProp("Data śmierci").onlyWithType[DMYearDate].map(_.year)

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

   //noinspection ScalaStyle
   private def liczbaDniOdŚmierciDoPochówku(rs: ResultSet): Seq[ResultRow] = {
      rs.filter(hasDate("Data śmierci") _ && hasDate("Data pochówku"))
         .countHorizontal(OldComputedGroupBy(
         getter = (dmv: DMValue) => {
            val diff: Option[Int] = for {
               bdatefield <- path"Data śmierci"(dmv).asType[DMYearDate]
               bdate <- bdatefield.fullDate
               cdatefield <- path"Data pochówku"(dmv).asType[DMYearDate]
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

   private def grupujPoOsobie(path: String): ResultSet => ResultSet =
      (rs: ResultSet) => rs.groupBy(OldComputedGroupBy(
         getter = (d: DMValue) => (for {
            imię <- path"$path.Imię" (d).asString
            nazwisko <- path"$path.Nazwisko" (d).asString
         } yield DMString(imię + " " + nazwisko)).getOrElse(DMNull),
         orderMapping = _.asString.getOrElse(""),
         appendColumnMode = CustomAppendColumn(path)
      ))

   private def przyczynyZgonówPoziomo(rs: ResultSet): Seq[ResultRow] =
      rs.countHorizontal(GroupBy("Przyczyna zgonu"))

   private def przyczynyZgonówPionowo(rs: ResultSet): Seq[ResultRow] =
      rs.groupBy(GroupBy("Przyczyna zgonu", PopularitySorted(Descending))).aggregateClassic("Liczba" -> ClassicAggregations.count)

   override val groupedQueries: Map[String, Query] = Map(
      "Sezonowość tygodniowa pogrzebów" -> Query(DataPochówku, (rs: ResultSet) => rs.countHorizontal(groupByWeekday("Data pochówku"))),
      "Najczęstsze przyczyny zgonów" -> Query(DataŚmierci, przyczynyZgonówPionowo),
      "Przyczyny zgonów" -> Query(DataŚmierci, przyczynyZgonówPoziomo),
      "Liczba dni od zgonu do pochówku" -> Query(DataPochówku, liczbaDniOdŚmierciDoPochówku),
      "Sezonowość miesięczna zgonów" -> Query(DataŚmierci, (rs: ResultSet) => rs.countHorizontal(groupByMonth("Data śmierci"))),
      "Zgony rocznie" -> Query(DataŚmierci, (rs: ResultSet) => rs.aggregateClassic("Liczba" -> ClassicAggregations.count))
   )

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
