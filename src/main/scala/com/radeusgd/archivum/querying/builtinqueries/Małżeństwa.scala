package com.radeusgd.archivum.querying.builtinqueries

import cats.implicits._
import com.radeusgd.archivum.datamodel.LiftDMValue._
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying.CustomGroupBy._
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.utils.Pipe._

class Małżeństwa(
    years: Int,
    folderGroupings: Seq[String],
    charakter: Option[String] = None
) extends BuiltinQuery(years, folderGroupings, charakter) {

  private def zakresDat(rs: ResultSet): Seq[ResultRow] = {
    def getYears(objs: Seq[DMValue]): Seq[Int] =
      objs.getProp("Data ślubu").onlyWithType[DMYearDate].map(_.year)

    rs.groupBy(GroupBy("Miejscowość"))
      .aggregate((objs: Seq[DMValue]) => {
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
    DMUtils
      .makeGetter(path)(dMValue)
      .asType[DMYearDate]
      .exists(_.fullDate.isDefined)

  private def grupujPoOsobie(path: String): ResultSet => ResultSet =
    (rs: ResultSet) =>
      rs.groupBy(
        OldComputedGroupBy(
          getter = (d: DMValue) =>
            (for {
              imię <- path"$path.Imię" (d).asString
              nazwisko <- path"$path.Nazwisko" (d).asString
            } yield DMString(imię + " " + nazwisko)).getOrElse(DMNull),
          orderMapping = _.asString.getOrElse(""),
          appendColumnMode = CustomAppendColumn(path)
        )
      )

  private def stanCywilny(rs: ResultSet): Seq[ResultRow] = {
    val stanCywilnyPana = path"Pan młody.Stan cywilny"
    val stanCywilnyPanny = path"Panna młoda.Stan cywilny"
    rs.countHorizontal(
      OldComputedGroupBy(
        getter = (d: DMValue) => {
          stanCywilnyPana(d).toString + " i " + stanCywilnyPanny(d)
        },
        orderMapping = _.toString
      )
    )
  }

  private def pochodzenie(rs: ResultSet): Seq[ResultRow] = {
    val pochodzeniePana = path"Pan młody.Pochodzenie"
    val pochodzeniePanny = path"Panna młoda.Pochodzenie"

    val counts = rs.countHorizontal(
      OldComputedGroupBy(
        getter = (d: DMValue) => {
          pochodzeniePana(d).toString + " / " + pochodzeniePanny(d)
        },
        orderMapping = _.toString
      )
    )

    counts.addHeader("Pochodzenie Pana młodego / Panny młodej")
  }

  private def płećŚwiadków(rs: ResultSet): Seq[ResultRow] = {
    rs.unpackAggregate("Świadkowie").countHorizontal(GroupBy("Płeć"))
  }

  private def zawódŚwiadków(rs: ResultSet): Seq[ResultRow] = {
    rs.unpackAggregate("Świadkowie").countHorizontal(GroupBy("Zawód"))
  }

  private def statusSpołecznyŚwiadków(rs: ResultSet): Seq[ResultRow] =
    rs.unpackAggregate("Świadkowie")
      .countHorizontal(GroupBy("Status społeczny"))

  private def najczęstsiŚwiadkowie(rs: ResultSet): Seq[ResultRow] = {
    rs.unpackAggregate("Świadkowie")
      .groupBy(
        ComputedGroupBy(
          (v: DMValue) => {
            val pref = path"Imię" (v).asString
              .getOrElse("") + " " + path"Nazwisko" (v).asString.getOrElse("")
            val spouse = path"Małżonek" (v)
            if (
              path"Płeć" (v) == DMString("K") && !spouse.asString
                .getOrElse("")
                .isEmpty
            ) {
              pref + " (mąż " + spouse + ")"
            } else {
              pref
            }
          },
          orderMapping = (_, s) => -s.length,
          defaultColumnName = "Świadek"
        )
      )
      .filterGroups(_.asSingleton.value.length >= 2)
      .countAfterGrouping()
  }

  private def najczęstsiŚwiadkowieTop20(rs: ResultSet): Seq[ResultRow] = {
    rs.unpackAggregate("Świadkowie")
      .groupBy(
        ComputedGroupBy(
          (v: DMValue) => {
            val pref = path"Imię" (v).asString
              .getOrElse("") + " " + path"Nazwisko" (v).asString.getOrElse("")
            val spouse = path"Małżonek" (v)
            if (
              path"Płeć" (v) == DMString("K") && !spouse.asString
                .getOrElse("")
                .isEmpty
            ) {
              pref + " (mąż " + spouse + ")"
            } else {
              pref
            }
          },
          orderMapping = (_, s) => -s.length,
          defaultColumnName = "Świadek"
        )
      )
      .filterTop(20)
      .countAfterGrouping()
  }

  private def wiekNowożeńców(rs: ResultSet): Seq[ResultRow] = {
    rs.groupByHorizontal(
        OldComputedGroupBy(
          (v: DMValue) => {
            val pan = path"Pan młody.Wiek" (v).asInt
            val pani = path"Panna młoda.Wiek" (v).asInt
            val normalnie = for {
              wpan <- pan
              wpani <- pani
            } yield
              if (wpan == wpani) "oboje nupturientów w tym samym wieku"
              else if (wpan > wpani) "mężczyzna starszy od kobiety"
              else "kobieta starsza od mężczyzny"

            val res = normalnie.getOrElse(
              if (pan.isEmpty && pani.isEmpty)
                "nieokreślony wiek obojga nupturientów"
              else "nieokreślony wiek jednego z nupturientów"
            )

            DMString(res)
          },
          _.asString.get
        )
      )
      .countAfterGrouping()
  }

  private def strukturaWyznaniowa(rs: ResultSet): Seq[ResultRow] = {
    rs.groupByHorizontal(
        crossGrouping("Pan młody.Wyznanie", "Panna młoda.Wyznanie")
      )
      .countAfterGrouping()
  }

  private def liczbaŚwiadków(rs: ResultSet): Seq[ResultRow] = {
    rs.countHorizontal(
        CustomGroupBy(
          "Świadkowie.length",
          identity,
          _.asInt.get
        )
      )
      .addHeader("Liczba świadków")
  }

  private def podzielNowożeńców(rs: ResultSet): ResultSet = {
    rs.deepFlatMap(d => {
      val pan = path"Pan młody" (d)
      val pani = path"Panna młoda" (d)
      val p1 = pan.asType[DMStruct].map(_.updated("Płeć", "M"))
      val p2 = pani.asType[DMStruct].map(_.updated("Płeć", "K"))
      val r: Seq[DMValue] = Seq(p1, p2).flatten
      r
    })
  }

  private def wyznaniePłeć(rs: ResultSet): Seq[ResultRow] = {
    podzielNowożeńców(rs)
      .groupBy(GroupBy("Wyznanie"))
      .countHorizontal(GroupBy("Płeć"))
  }

  private def wiekStan(rs: ResultSet): Seq[ResultRow] = {

    val podziałWieku = ComputedGroupBy(
      (v: DMValue) => {
        val wiek = path"Wiek" (v)
        wiek.asInt
          .map(wiek => {
            if (wiek < 15) new DMString("<15") {
              override def compare(that: DMOrdered): Int = -1
            }
            else if (wiek >= 85) new DMString(">=85") {
              override def compare(that: DMOrdered): Int =
                that match {
                  case DMString("brak danych") => -1
                  case _                       => 1
                }
            }
            else {
              val low = (wiek / 5) * 5
              val top = low + 4
              DMString("" + low + "-" + top)
            }
          })
          .getOrElse("brak danych")
      },
      (k: DMValue, _) => {
        k.asInstanceOf[DMOrdered]
      }
    )

    val podziałPłci = GroupBy("Płeć")
    val podziałStanu = GroupBy("Stan cywilny")

    podzielNowożeńców(rs)
      .groupBy(podziałPłci)
      .groupBy(podziałStanu)
      .countHorizontal(podziałWieku)
      .addHeader("Przedział wiekowy")
  }

  private def zgodaOjca(rs: ResultSet): Seq[ResultRow] =
    rs.countHorizontal(GroupBy("Zgoda na zawarcie małżeństwa"))

  private def tylkoTaSamaParafia(rs: ResultSet): ResultSet = {
    val same = DMString("Ta sama parafia")
    rs.filter(
      path"Pan młody.Pochodzenie" === same && path"Panna młoda.Pochodzenie" === same
    )
  }

  private def liczbaWTejSamejParafii(rs: ResultSet): Seq[ResultRow] =
    tylkoTaSamaParafia(rs).countAfterGrouping("Liczba ślubów")

  private def miejscowościWTejSamejParafii(rs: ResultSet): Seq[ResultRow] =
    tylkoTaSamaParafia(rs)
      .countHorizontal(
        crossGrouping(
          "Pan młody.Miejsce urodzenia",
          "Panna młoda.Miejsce urodzenia"
        )
      )
      .addHeader("Miejscowości")

  private def pochodzenieWedługPłci(rs: ResultSet): Seq[ResultRow] =
    podzielNowożeńców(rs)
      .groupByHorizontal(GroupBy("Płeć"))
      .countHorizontal(GroupBy("Pochodzenie"))

  private def płećWTejSamejParafii(rs: ResultSet): Seq[ResultRow] = {
    def zParafii(v: DMValue): Option[Boolean] =
      v match {
        case DMString("Ta sama parafia") => Some(true)
        case DMString("Spoza parafii")   => Some(false)
        case _                           => None
      }

    rs.countHorizontal(
      ComputedGroupBy(
        (v: DMValue) => {
          val r = for {
            panLokal <- zParafii(path"Pan młody.Pochodzenie" (v))
            paniLokal <- zParafii(path"Panna młoda.Pochodzenie" (v))
          } yield (panLokal, paniLokal) match {
            case (true, true) =>
              val miejscowośćPan =
                path"Pan młody.Miejsce urodzenia" (v).asString.get
              val miejscowośćPani =
                path"Panna młoda.Miejsce urodzenia" (v).asString.get
              if (miejscowośćPan.isEmpty || miejscowośćPani.isEmpty) None
              else if (miejscowośćPan == miejscowośćPani)
                Some("Mężczyzna i kobieta z jednej miejscowości parafialnej")
              else Some("Mężczyzna i kobieta z dwóch miejscowości parafialnych")
            case (true, false)  => Some("Mężczyzna spoza parafii")
            case (false, true)  => Some("Kobieta spoza parafii")
            case (false, false) => Some("Mężczyzna i kobieta spoza parafii")
          }

          DMString(r.flatten.getOrElse("Brak informacji"))
        },
        (k: DMValue, _) => k.asInstanceOf[DMOrdered]
      )
    )
  }

  private def udziałPozaparafialnych(rs: ResultSet): Seq[ResultRow] = {
    rs.groupBy(GroupBy("Miejscowość"))
      .groupBy(GroupBy("Parafia"))
      .countHorizontal(GroupByYears("Data ślubu", years))
      .addHeader("Liczba przybyłych")
  }

  override val groupedQueries: Map[String, Query] = Map(
    "Sezonowość tygodniowa zawieranych małżeństw" -> Query(
      DataŚlubu,
      (rs: ResultSet) => rs.countHorizontal(groupByWeekday("Data ślubu"))
    ),
    "Sezonowość miesięczna zawieranych małżeństw" -> Query(
      DataŚlubu,
      (rs: ResultSet) => rs.countHorizontal(groupByMonth("Data ślubu"))
    ),
    "Sezonowość roczna zawieranych małżeństw" -> Query(
      DataŚlubu,
      (rs: ResultSet) =>
        rs.aggregateClassic("Liczba" -> ClassicAggregations.count)
    ),
    "Małżeństwa według stanu cywilnego nowożeńców" -> Query(
      DataŚlubu,
      stanCywilny
    ),
    "Pochodzenie małżonków" -> Query(DataŚlubu, pochodzenie),
    "Płeć świadków" -> Query(DataŚlubu, płećŚwiadków),
    "Zawód świadków" -> Query(DataŚlubu, zawódŚwiadków),
    "Status społeczny świadków" -> Query(DataŚlubu, statusSpołecznyŚwiadków),
    "Osoby będące najczęściej świadkami małżeństw w badanych okresach" -> Query(
      DataŚlubu,
      najczęstsiŚwiadkowie
    ),
    "Wiek nowożeńców" -> Query(DataŚlubu, wiekNowożeńców),
    "Liczba świadków małżeństwa" -> Query(DataŚlubu, liczbaŚwiadków),
    "Struktura wyznaniowa zawieranych małżeństw" -> Query(
      DataŚlubu,
      strukturaWyznaniowa
    ),
    "Zgoda ojca na zawarcie małżeństwa" -> Query(DataŚlubu, zgodaOjca),
    "Liczba małżeństw w obrębie tej samej parafii" -> Query(
      DataŚlubu,
      liczbaWTejSamejParafii
    ),
    "Małżeństwa w obrębie tej samej parafii (miejscowości)" -> Query(
      DataŚlubu,
      miejscowościWTejSamejParafii
    ),
    "Pochodzenie nowożeńców według płci" -> Query(
      DataŚlubu,
      pochodzenieWedługPłci
    ),
    "Małżeństwa w obrębie tej samej parafii (płeć)" -> Query(
      DataŚlubu,
      płećWTejSamejParafii
    )
  )

  override val ungroupedQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
    "Osoby będące najczęściej świadkami małżeństw (najczęściej występujące)" -> najczęstsiŚwiadkowieTop20,
    "Wyznanie nowożeńców z podziałem na płeć" -> wyznaniePłeć,
    "Struktura wieku nowożeńców z uwzględnieniem ich stanu cywilnego" -> wiekStan
  )

  override val manualQueries: Map[String, ResultSet => Seq[ResultRow]] = Map(
    // "daty" -> zakresDat,
    "Udział nowożeńców z miejscowości pozaparafialnych w małżeństwach zawartych w parafii" -> udziałPozaparafialnych
  )
}
