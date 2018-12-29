package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel.{DMStruct, Model}
import com.radeusgd.archivum.querying.{Grouping, MultipleResultRow, ResultSet}

trait Repository {
   type Rid = Long

   def model: Model

   def createRecord(value: DMStruct): Rid

   def fetchRecord(rid: Rid): Option[DMStruct]

   def updateRecord(rid: Rid, newValue: DMStruct): Unit

   def deleteRecord(rid: Rid): Unit

   // path can contain * indicating all elements from array
   // it's an imperfect way to implement this, but time budget is too small
   def fetchAutocompletions(path: Seq[String], hint: String, limit: Int = 10): Seq[String]

   // TODO using this in processing will be slow, in the future we should extend Repository to handle Streams of records or something similar
   def fetchAllRecords(): Seq[(Rid, DMStruct)]

   def fetchAllGrouped(groups: Grouping*): ResultSet = {
      val base = fetchAllRecords().map(_._2)
      val all = ResultSet(Seq(MultipleResultRow(base)))

      groups.foldLeft(all)(_.groupBy(_))
   }

   def ridSet: RidSetHelper

   def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)]
}