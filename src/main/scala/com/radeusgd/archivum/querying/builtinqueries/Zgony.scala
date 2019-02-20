package com.radeusgd.archivum.querying.builtinqueries

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

   private def grupujPoOsobie(path: String): ResultSet => ResultSet =
      (rs: ResultSet) => rs.groupBy(ComputedGroupBy(
         getter = (d: DMValue) => (for {
            imię <- path"$path.Imię" (d).asString
            nazwisko <- path"$path.Nazwisko" (d).asString
         } yield DMString(imię + " " + nazwisko)).getOrElse(DMNull),
         orderMapping = _.asString.getOrElse(""),
         appendColumnMode = CustomAppendColumn(path)
      ))

   private def przyczynyZgonów(rs: ResultSet): Seq[ResultRow] =
      rs.groupBy(GroupBy("Przyczyna zgonu")).aggregateClassic("Liczba" -> ClassicAggregations.count)

   override val groupedQueries: Map[String, Query] = Map(
      "Sezonowość tygodniowa pogrzebów" -> Query(DataPochówku, (rs: ResultSet) => rs.countWithPercentages(groupByWeekday("Data pochówku"))),
      "Najczęstsze przyczyny zgonów" -> Query(DataŚmierci, przyczynyZgonów)
   )

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
