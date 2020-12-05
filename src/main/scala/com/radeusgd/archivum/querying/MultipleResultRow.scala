package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.utils.BetterTuples._

case class MultipleResultRow(
    prefix: ResultRow,
    objects: NestedMapADT[String, Seq[DMValue]]
) {
  /*
   Filters contained objects with given predicate.
   Resulting row may be empty (just a prefix).
   */
  def filter(predicate: DMValue => Boolean): MultipleResultRow =
    MultipleResultRow(prefix, objects.map(_.filter(predicate)))

  //noinspection ScalaStyle
  def groupBy(grouping: Grouping): Seq[MultipleResultRow] = {
    def alterPrefix(groupName: DMValue): ResultRow = {
      grouping.appendColumnMode match {
        case DoNotAppend => prefix
        case Default     => prefix.updated(grouping.defaultColumnName, groupName)
        case CustomAppendColumn(columnName, mapping) =>
          prefix.updated(columnName, mapping(groupName))
      }
    }

    objects match {
      case NestedMapElement(value) =>
        val sorted = grouping.groupDMs(value)
        sorted.map({
          case (groupName, elems) =>
            MultipleResultRow(alterPrefix(groupName), elems)
        })
      case NestedMap(mapping) =>
        ??? // FIXME this is hard but possible, need to gather into groups in each leaf of NestedMap, than merge these groups by the grouping into rows and put them back into NestedMap, filling missing places with empty lists
    }
  }

  def groupByHorizontal(grouping: Grouping): MultipleResultRow = {
    MultipleResultRow(
      prefix,
      objects.flatMap((objs: Seq[DMValue]) => {
        grouping
          .groupDMs(objs)
          .foldLeft[NestedMap[String, Seq[DMValue]]](NestedMap.empty)({
            case (nm, (key, vals)) =>
              nm.updated(Grouping.groupkeyToString(key), vals)
          })
      })
    )
  }

  def groupByHorizontal(
      grouping: Grouping,
      presetGroupings: Seq[String]
  ): MultipleResultRow = {
    MultipleResultRow(
      prefix,
      objects.flatMap((objs: Seq[DMValue]) => {
        val grouped: Map[String, Seq[DMValue]] =
          grouping
            .groupDMs(objs)
            .mapFirst(Grouping.groupkeyToString)
            .toMap
            .withDefault(_ => Nil)

        presetGroupings
          .foldLeft[NestedMap[String, Seq[DMValue]]](NestedMap.empty)({
            case (nm, key) => nm.updated(key, grouped(key))
          })
      })
    )
  }

  def countHorizontal(
      grouping: Grouping,
      presetGroupings: Seq[String],
      includePercentages: Boolean,
      appendHeader: Option[String]
  ): ResultRow = {
    val countedRows = objects.flatMap[DMValue]((objs: Seq[DMValue]) => {
      val allCount = objs.length
      val grouped: Map[String, Seq[DMValue]] =
        grouping
          .groupDMs(objs)
          .mapFirst(Grouping.groupkeyToString)
          .toMap
          .withDefault(_ => Nil)

      val reordered = presetGroupings
        .foldLeft[NestedMap[String, Seq[DMValue]]](NestedMap.empty)({
          case (nm, key) => nm.updated(key, grouped(key))
        })

      if (includePercentages)
        reordered.flatMap((objs: Seq[DMValue]) =>
          ResultRow(
            "l.b." -> DMInteger(objs.length),
            "%" -> percentage(objs.length, allCount)
          )
        )
      else
        reordered.flatMap1((objs: Seq[DMValue]) =>
          NestedMapADT.singleton(DMInteger(objs.length))
        )
    })

    val newRows = appendHeader match {
      case Some(header) => countedRows.withHeader(header)
      case None         => countedRows
    }
    prefix.append(newRows)
  }

  def aggregate(
      aggregations: Seq[(String, Seq[DMValue] => DMValue)]
  ): ResultRow = {
    prefix.append(
      objects.flatMap((objs: Seq[DMValue]) =>
        aggregations.foldLeft[ResultRow](NestedMap.empty) {
          case (agg: ResultRow, (name, f)) => agg.updated(name, f(objs))
        }
      )
    )
  }

  def aggregate(f: Seq[DMValue] => ResultRow): ResultRow = {
    prefix.append(objects.flatMap(f))
  }

  override def toString: String = "MultipleResultRow(" + prefix + ", ...)"

  def objectsShape: NestedMapADT[String, Unit] = objects.map(_ => ())
}

object MultipleResultRow {
  def apply(prefix: ResultRow, objects: Seq[DMValue]): MultipleResultRow =
    new MultipleResultRow(prefix, NestedMapElement(objects))
  def apply(objects: Seq[DMValue]): MultipleResultRow =
    MultipleResultRow(NestedMap.empty[String, DMValue], objects)
}
