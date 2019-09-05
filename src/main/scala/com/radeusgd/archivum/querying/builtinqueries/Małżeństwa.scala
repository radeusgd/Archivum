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
      (rs: ResultSet) => rs.groupBy(OldComputedGroupBy(
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
      rs.countHorizontal(OldComputedGroupBy(
         getter = (d: DMValue) => {
            stanCywilnyPana(d).toString + " i " + stanCywilnyPanny(d)
         },
         orderMapping = _.toString
      ))
   }

   private def pochodzenie(rs: ResultSet): Seq[ResultRow] = {
      val pochodzeniePana = path"Pan młody.Pochodzenie"
      val pochodzeniePanny = path"Panna młoda.Pochodzenie"

      val counts = rs.countHorizontal(OldComputedGroupBy(
         getter = (d: DMValue) => {
            pochodzeniePana(d).toString + " / " + pochodzeniePanny(d)
         },
         orderMapping = _.toString
      ))

      counts.map(ResultRow.addHeader(_, "Pochodzenie Pana młodego / Panny młodej"))
   }

   private def płećŚwiadków(rs: ResultSet): Seq[ResultRow] = {
      rs.unpackAggregate("Świadkowie").countHorizontal(GroupBy("Płeć"))
   }

   private def zawódŚwiadków(rs: ResultSet): Seq[ResultRow] = {
      rs.unpackAggregate("Świadkowie").countHorizontal(GroupBy("Zawód"))
   }

   private def najczęstsiŚwiadkowie(rs: ResultSet): Seq[ResultRow] = {
      rs.unpackAggregate("Świadkowie")
         .groupBy(ComputedGroupBy(
            (v: DMValue) => {
               val pref = path"Imię" (v).asString.getOrElse("") + " " + path"Nazwisko" (v).asString.getOrElse("")
               val spouse = path"Małżonek" (v)
               if (path"Płeć" (v) == DMString("K") && !spouse.asString.getOrElse("").isBlank) {
                  pref + " (mąż " + spouse + ")"
               } else {
                  pref
               }
            },
            orderMapping = (_, s) => -s.length,
            defaultColumnName = "Świadek"
         )).filterGroups(_.asSingleton.value.length >= 2).countAfterGrouping()
   }

   private def najczęstsiŚwiadkowieTop20(rs: ResultSet): Seq[ResultRow] = {
      rs.ungroup()
         .unpackAggregate("Świadkowie")
         .groupBy(ComputedGroupBy(
            (v: DMValue) => {
               val pref = path"Imię" (v).asString.getOrElse("") + " " + path"Nazwisko" (v).asString.getOrElse("")
               val spouse = path"Małżonek" (v)
               if (path"Płeć" (v) == DMString("K") && !spouse.asString.getOrElse("").isBlank) {
                  pref + " (mąż " + spouse + ")"
               } else {
                  pref
               }
            },
            orderMapping = (_, s) => -s.length,
            defaultColumnName = "Świadek"
         )).filterTop(20).countAfterGrouping()
   }

   private def wiekNowożeńców(rs: ResultSet): Seq[ResultRow] = {
      rs.groupByHorizontal(OldComputedGroupBy(
         (v: DMValue) => {
            val pan = path"Pan młody.Wiek"(v).asInt
            val pani = path"Panna młoda.Wiek"(v).asInt
            val normalnie = for {
               wpan <- pan
               wpani <- pani
            } yield if (wpan == wpani) "oboje nupturientów w tym samym wieku"
            else if (wpan > wpani) "mężczyzna starszy od kobiety"
            else "kobieta starsza od mężczyzny"

            val res = normalnie.getOrElse(
               if (pan.isEmpty && pani.isEmpty) "nieokreślony wiek obojga nupturientów"
               else "nieokreślony wiek jednego z nupturientów"
            )

            DMString(res)
         },
         _.asString.get
      )).countAfterGrouping()
   }

   private def strukturaWyznaniowa(rs: ResultSet): Seq[ResultRow] = {
      rs.groupByHorizontal(OldComputedGroupBy(
         (v: DMValue) => {
            val pan = path"Pan młody.Wyznanie"(v).asString.get
            val pani = path"Panna młoda.Wyznanie"(v).asString.get
            DMString(pan + " - " + pani)
         },
         _.asString.get
      )).countAfterGrouping()
   }



   override val groupedQueries: Map[String, Query] = Map(
      "Sezonowość tygodniowa zawieranych małżeństw" -> Query(DataŚlubu, (rs: ResultSet) => rs.countHorizontal(groupByWeekday("Data ślubu"))),
      "Sezonowość miesięczna zawieranych małżeństw" -> Query(DataŚlubu, (rs: ResultSet) => rs.countHorizontal(groupByMonth("Data ślubu"))),
      "Sezonowość roczna zawieranych małżeństw" -> Query(DataŚlubu, (rs: ResultSet) => rs.aggregateClassic("Liczba" -> ClassicAggregations.count)),
      "Małżeństwa według stanu cywilnego nowożeńców" -> Query(DataŚlubu, stanCywilny),
      "Pochodzenie małżonków" -> Query(DataŚlubu, pochodzenie),
      "Płeć świadków" -> Query(DataŚlubu, płećŚwiadków),
      "Zawód świadków" -> Query(DataŚlubu, zawódŚwiadków),
      "Osoby będące najczęściej świadkami małżeństw w badanych okresach" -> Query(DataŚlubu, najczęstsiŚwiadkowie),
      "Wiek nowożeńców" -> Query(DataŚlubu, wiekNowożeńców),
      "Osoby będące najczęściej świadkami małżeństw (najczęściej występujące)" -> Query(DataŚlubu, najczęstsiŚwiadkowieTop20)
   )

   override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
      "daty" -> zakresDat
   )
}
