package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMStruct, DMUtils, DMValue}

case class ResultSet(rows: Seq[MultipleResultRow]) {
   def getResult: Seq[ResultRow] = rows.map(_.prefix)

   def filter(predicate: DMValue => Boolean): ResultSet =
      ResultSet(rows.map(_.filter(predicate)))

   /*
   See MultipleResultRow for description.
    */
   def unpackArray(path: String): ResultSet =
      ResultSet(rows.map(_.unpackArray(path)))


   def flatten(): ResultSet = {
      def unpackRow(row: MultipleResultRow): Seq[MultipleResultRow] =
         row.objects.map(obj => MultipleResultRow(row.prefix, Seq(obj)))
      ResultSet(rows.flatMap(unpackRow))
   }

   def flatMap(f: MultipleResultRow => Seq[MultipleResultRow]): ResultSet = {
      ResultSet(rows.flatMap(f))
   }

   /*
   Groups results contained in each of MultipleResultRows into separate, smaller MultipleResultRows.
    */
   def groupBy(grouping: Grouping): ResultSet = {
      flatMap(_.groupBy(grouping))
   }

   //def sortBy[A](sorter: MultipleResultRow => A)

   def aggregate(aggregations: (String, Seq[DMValue] => DMValue)*): Seq[ResultRow] = {
      rows.map(_.aggregate(aggregations))
   }

   def countGroups(path: String, countColumn: String): Seq[ResultRow] =
      countGroups(path, path, countColumn)

   def countGroups(path: String, nameColumn: String, countColumn: String): Seq[ResultRow] =
      groupBy(GroupBy(path, PopularitySorted(Descending), CustomAppendColumn(nameColumn))).aggregate(
         countColumn -> Aggregations.count
      )

   def countTransposed(path: String, traits: Seq[(String, DMValue)], default: Option[String]): Seq[ResultRow] =
      aggregate(Aggregations.countTransposed(path, traits, default):_*)
}
