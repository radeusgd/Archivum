package com.radeusgd.archivum.querying

import cats.kernel.Semigroup
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.utils.BetterTuples._

case class ResultSet(rows: Seq[MultipleResultRow]) {

   // discards nested results and returns just the current prefix
   def cut: Seq[ResultRow] = rows.map(_.prefix)

   def filter(predicate: DMValue => Boolean): ResultSet =
      ResultSet(rows.map(_.filter(predicate)))

   def filterGroups(predicate: NestedMapADT[String, Seq[DMValue]] => Boolean): ResultSet =
      ResultSet(rows.filter(mrr => predicate(mrr.objects)))

   def map(f: MultipleResultRow => MultipleResultRow): ResultSet =
      ResultSet(rows.map(f))

   def deepMap(f: DMValue => DMValue): ResultSet =
      ResultSet(rows.map(mrr => MultipleResultRow(mrr.prefix, mrr.objects.map(_.map(f)))))

   def flatMap(f: MultipleResultRow => Seq[MultipleResultRow]): ResultSet = {
      ResultSet(rows.flatMap(f))
   }

   def deepFlatMap(f: DMValue => Seq[DMValue]): ResultSet = {
      ResultSet(rows.map(mrr => MultipleResultRow(mrr.prefix, mrr.objects.map(_.flatMap(f)))))
   }

   // replaces each ResultRow with (0 or many) elements from some Array field, bins are not modified
   def unpackAggregate(path: String): ResultSet = {
      val get = DMUtils.makeGetter(path)
      deepFlatMap(o => get(o).asType[DMArray].map(_.values).getOrElse(Nil))
   }

   // replaces each ResultRow with (0 or many) elements from some Array field, elements from each object get a separate bin (empty objects are discarded)
   //def unpackAggregateSeparate(path: String): ResultSet

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

   def countGroups(path: String, countColumnName: String): Seq[ResultRow] =
      countGroups(path, path, countColumnName)

   def countGroups(path: String, nameColumn: String, countColumnName: String): Seq[ResultRow] =
      groupBy(GroupBy(path, PopularitySorted(Descending), CustomAppendColumn(nameColumn))).aggregateClassic(
         countColumnName -> ClassicAggregations.count
      )

   def countTransposed(path: String, traits: Seq[(String, DMValue)], default: Option[String]): Seq[ResultRow] =
      aggregateClassic(ClassicAggregations.countTransposed(path, traits, default):_*)

   def countHorizontal(grouping: Grouping, includePercentages: Boolean = false): Seq[ResultRow] = {
      val preset = computeHorizontalGroupingPreset(grouping)
      rows.map(row => row.countHorizontal(grouping, preset, includePercentages))
   }

   def countAfterGrouping(): Seq[ResultRow] = {
      rows.map(_.aggregate(objs => ResultRow("l.b." -> DMInteger(objs.length))))
   }

   def filterTop(howmany: Int): ResultSet = {
      val amounts = rows.map(_.objects.asSingleton.value.length)
      if (amounts.isEmpty) this // if there are no records, don't bother
      else {
         val smallest = amounts.sorted.reverse.take(howmany).last
         val r = rows.filter(_.objects.asSingleton.value.length >= smallest)
         ResultSet(r)
      }
   }

   // 'flatten' - merges all groups in the ResultSet into one big group
   // also drops the prefix (because it can differ for different group)
   def ungroup(): ResultSet = {
      val objs = rows.map(_.objects)
      val merged = objs.reduce(NestedMapADT.merge(_,_)((x: Seq[DMValue], y: Seq[DMValue]) => x ++ y))
      ResultSet(Seq(MultipleResultRow(ResultRow.empty, merged)))
   }

}
