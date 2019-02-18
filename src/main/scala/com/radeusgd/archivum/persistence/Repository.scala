package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.querying.{Grouping, MultipleResultRow, ResultSet}
import com.radeusgd.archivum.utils.BetterTuples._

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

   def fetchAll(): ResultSet =
      ResultSet(Seq(MultipleResultRow(fetchAllRecords().extractSeconds)))

   // this should be overriden with a faster implementation
   def fetchAllGrouped(filter: SearchCriteria, groups: Grouping*): ResultSet = {
      println("WARNING! Using a slow implementation! (GROUP BY)")
      val base = searchRecords(filter).map(_._2)
      val all = ResultSet(Seq(MultipleResultRow(base)))

      groups.foldLeft(all)(_.groupBy(_))
   }

   def fetchAllGrouped(groups: Grouping*): ResultSet = fetchAllGrouped(Truth, groups: _*)

   def ridSet: RidSetHelper

   // this should be overriden with a faster implementation
   def searchRecords(criteria: SearchCriteria): Seq[(Rid, DMStruct)] = {
      println("WARNING! Using a very slow implementation! (FILTER)")
      val all = fetchAllRecords()
      val pred: DMValue => Boolean = SearchCriteria.makePredicate(criteria)
      all.filter(t => pred(t._2))
   }

   def search(criteria: SearchCriteria): ResultSet =
      ResultSet(Seq(MultipleResultRow(searchRecords(criteria).extractSeconds)))

   def searchIds(criteria: SearchCriteria): Seq[Rid] = {
      println("Warning! using a very slow implementation of searchIds!!!")
      searchRecords(criteria).extractFirsts
   }

   // TODO  not sure if filter should be handled natively, but for now it makes sense
   def fullTextSearch(text: String, filter: SearchCriteria): Seq[(Rid, DMStruct)] = {
      println("Warning, using a slow fulltext implementation")

      val frags = text.split(' ').filter(s => !s.isEmpty)

      def areAllFragsPresentSomewhere(strings: Seq[String]): Boolean =
         frags.forall(frag =>
            strings.exists(str =>
               str.contains(frag)
            )
         )

      searchRecords(filter).filter({
         case (rid, dmroot) =>
            val strings = model.roottype.extractAllStrings(dmroot)
            areAllFragsPresentSomewhere(strings)
      })
   }

   def fetchIds(ids: Seq[Rid]): Seq[(Rid, DMStruct)] = {
      for {
         rid <- ids
         record <- fetchRecord(rid)
      } yield (rid, record)
   }

   @deprecated // TODO not sure if want this function
   def getAllDistinctValues(path: List[String], filter: SearchCriteria = Truth): List[DMValue] = {
      println("WARNING! Using a very slow implementation! (DISTINCT)")
      searchRecords(filter).map(_._2).map(DMUtils.makeGetter(path)).toList.distinct
   }
}