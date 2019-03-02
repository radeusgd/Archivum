package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.utils.BetterTuples._

case class ResultSet(rows: Seq[MultipleResultRow]) {
   def cutAggregation: Seq[ResultRow] = rows.map(_.prefix)

   def filter(predicate: DMValue => Boolean): ResultSet =
      ResultSet(rows.map(_.filter(predicate)))

   def map(f: MultipleResultRow => MultipleResultRow): ResultSet =
      ResultSet(rows.map(f))

   def flatMap(f: MultipleResultRow => Seq[MultipleResultRow]): ResultSet = {
      ResultSet(rows.flatMap(f))
   }

   /*
   Groups results contained in each of MultipleResultRows into separate, smaller MultipleResultRows.
    */
   def groupBy(grouping: Grouping): ResultSet = {
      flatMap(_.groupBy(grouping))
   }

   private def computeHorizontalGroupingPreset(grouping: Grouping): Seq[String] = {
      // TODO this can be optimized to not do the grouping twice
      val ALLTHERESULTS: Seq[DMValue] = rows.flatMap(_.objects.flatten).flatten
      val ALLGROUPED = grouping.groupDMs(ALLTHERESULTS)
      val preset: Seq[String] = ALLGROUPED.extractFirsts.map(Grouping.groupkeyToString)
      preset
   }

   def groupByHorizontal(grouping: Grouping): ResultSet = {
      val preset = computeHorizontalGroupingPreset(grouping)
      map(_.groupByHorizontal(grouping, preset))
   }

   //def sortBy[A](sorter: MultipleResultRow => A)

   def aggregateClassic(aggregations: (String, ClassicAggregations.Aggregation)*): Seq[ResultRow] = {
      rows.map(_.aggregate(aggregations))
   }

   def aggregate(f: Seq[DMValue] => ResultRow): Seq[ResultRow] = {
      rows.map(_.aggregate(f))
   }

   def countGroups(path: String, countColumn: String): Seq[ResultRow] =
      countGroups(path, path, countColumn)

   def countGroups(path: String, nameColumn: String, countColumn: String): Seq[ResultRow] =
      groupBy(GroupBy(path, PopularitySorted(Descending), CustomAppendColumn(nameColumn))).aggregateClassic(
         countColumn -> ClassicAggregations.count
      )

   def countTransposed(path: String, traits: Seq[(String, DMValue)], default: Option[String]): Seq[ResultRow] =
      aggregateClassic(ClassicAggregations.countTransposed(path, traits, default):_*)

   def countHorizontal(grouping: Grouping, includePercentages: Boolean = false): Seq[ResultRow] = {
      val preset = computeHorizontalGroupingPreset(grouping)
      rows.map(_.countHorizontal(grouping, preset, includePercentages))
   }

   def countAfterGrouping(): Seq[ResultRow] = {
      rows.map(_.aggregate(objs => ResultRow("l.b." -> DMInteger(objs.length))))
   }

}
