package com.radeusgd.archivum.querying.builtinqueries


import cats.implicits._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying.CustomGroupBy._
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.utils.Pipe._

class Małżeństwa(years: Int, folderGroupings: Seq[String], charakter: Option[String] = None)
   extends BuiltinQuery(years, folderGroupings, charakter) {

   private def zakresDat(rs: ResultSet): Seq[ResultRow] = {
      def getYears(objs: Seq[DMValue]): Seq[Int] =
         objs.getProp("Data ślubu").onlyWithType[DMYearDate].map(_.year)

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

   private def stanCywilny(rs: ResultSet): Seq[ResultRow] = {
      val stanCywilnyPana = path"Pan młody.Stan cywilny"
      val stanCywilnyPanny = path"Panna młoda.Stan cywilny"
      rs.countHorizontal(ComputedGroupBy(
         getter = (d: DMValue) => {
            stanCywilnyPana(d).toString + " i " + stanCywilnyPanny(d)
         },
         orderMapping = _.toString
      ))
   }

   override val groupedQueries: Map[String, Query] = Map(
      "Sezonowość tygodniowa ślubów" -> Query(DataŚlubu, (rs: ResultSet) => rs.countHorizontal(groupByWeekday("Data ślubu"))),
      "Sezonowość miesięczna ślubów" -> Query(DataŚlubu, (rs: ResultSet) => rs.countHorizontal(groupByMonth("Data ślubu"))),
      "Śluby rocznie" -> Query(DataŚlubu, (rs: ResultSet) => rs.aggregateClassic("Liczba" -> ClassicAggregations.count)),
      "Stan cywilny nupturientów" -> Query(DataŚlubu, stanCywilny)
   )

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
