package com.radeusgd.archivum.querying.builtinqueries

import java.nio.file.Paths

import com.radeusgd.archivum.datamodel.{DMString, DMUtils, DMValue}
import com.radeusgd.archivum.persistence._
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.querying.utils.XLSExport
import javafx.concurrent.Task

sealed abstract class YearGrouping {
   def prepareGroupings(years: Int): Seq[Grouping]
}
class DateBasedGrouping(path: String) extends YearGrouping {
   override def prepareGroupings(years: Int): Seq[Grouping] =
      GroupByYears(path, years, CustomAppendColumn("Rok")) :: Nil
}
object DataChrztu extends DateBasedGrouping("Data chrztu")
object DataUrodzenia extends DateBasedGrouping("Data urodzenia")
object DataŚlubu extends DateBasedGrouping("Data ślubu")
object DataŚmierci extends DateBasedGrouping("Data śmierci")
object DataPochówku extends DateBasedGrouping("Data pochówku")
object NoYearGrouping extends YearGrouping {
   override def prepareGroupings(years: Int): Seq[Grouping] = Nil
}

case class Query(yearGrouping: YearGrouping, realization: ResultSet => Seq[ResultRow]) {
   def withFilter(predicate: DMValue => Boolean): Query =
      Query(yearGrouping, (rs: ResultSet) => realization(rs.filter(predicate)))
}

//noinspection ScalaStyle
abstract class BuiltinQuery(years: Int, folderGroupings: Seq[String], charakter: Option[String]) {

   type ClassicQuery = ResultSet => Seq[ResultRow]
   val groupedQueries: Map[String, Query]
   val ungroupedQueries: Map[String, ClassicQuery]
   val manualQueries: Map[String, ClassicQuery]

   private def prepareYearGrouping(yearGrouping: YearGrouping): Seq[Grouping] =
      yearGrouping.prepareGroupings(years)

   private val fileExt: String = ".xlsx"

   def prepareTask(resultPath: String, repo: Repository): Task[Unit] = new Task[Unit]() {

      val charakterFilter: SearchCriteria =
         charakter.map(ch => Equal(Seq("Charakter miejscowości"), DMString(ch))).getOrElse(Truth)

      private def runManualQueries(workToDo: Int): Unit = {
         for (((qname, query), index) <- manualQueries.zipWithIndex) {
            val all = repo.search(charakterFilter)
            val res = query(all)

            XLSExport.export(resultPath + qname + fileExt, res)
            println(s"Query $qname written ${res.length} rows in total")
            updateProgress(index + 1, workToDo)
            if (isCancelled) throw new RuntimeException("Cancelled")
         }
      }

      override def call(): Unit = {
         val classicQueries: Map[String, Either[ClassicQuery, Query]] =
            ungroupedQueries.mapValues(Left(_)) ++ groupedQueries.mapValues(Right(_))

         if (folderGroupings.isEmpty) {
            val workToDo = classicQueries.size + manualQueries.size

            updateProgress(0, workToDo)

            runManualQueries(workToDo)

            for (((qname, queryDesc), index) <- classicQueries.zipWithIndex) {
               updateMessage("Running " + qname)
               val res: Seq[ResultRow] = queryDesc match {
                  case Right(Query(yg, query)) =>
                     val all = repo.fetchAllGrouped(charakterFilter, prepareYearGrouping(yg) : _*)
                     query(all)
                  case Left(query) =>
                     val all = repo.search(charakterFilter)
                     query(all)
               }
               XLSExport.export(resultPath + qname + fileExt, res)
               println(s"Query $qname written ${res.length} rows in total")
               updateProgress(manualQueries.size + index + 1, workToDo)
               if (isCancelled) throw new RuntimeException("Cancelled")
            }

            updateMessage("Done")
         } else {
            updateProgress(0, 1)
            val firstLevelGroupPath = DMUtils.parsePath(folderGroupings.head)
            val firstLevelGroupings = repo.getAllDistinctValues(firstLevelGroupPath, filter = charakterFilter)
            val flgLen = firstLevelGroupings.length
            val workToDo = classicQueries.size * (flgLen + 1) + 1 + manualQueries.size
            updateProgress(1, workToDo)

            runManualQueries(workToDo)

            var progress = manualQueries.size + 1
            updateProgress(progress, workToDo)
            println("Will run for " + firstLevelGroupings)

            for ((qname, queryDesc) <- classicQueries) {

               def runQuery(filter: SearchCriteria, subName: String, restGroupings: Seq[String]): Unit = {
                  if (restGroupings.isEmpty) {
                     val res = {
                        queryDesc match {
                           case Right(Query(yg, query)) =>
                              val all = repo.fetchAllGrouped(
                                 filter,
                                 prepareYearGrouping(yg):_*
                              )
                              query(all)
                           case Left(query) =>
                              val all = repo.search(
                                 filter
                              )
                              query(all)
                        }
                     }
                     val path = Paths.get(resultPath).resolve(subName).resolve(qname + ".xlsx").toString
                     XLSExport.export(path, res)
                     println(s"Written ${res.length} rows to $path")
                  } else {
                     val groupPath = DMUtils.parsePath(restGroupings.head)
                     val groupings = repo.getAllDistinctValues(groupPath, filter)
                     for (groupValue <- groupings) {
                        runQuery(
                           And(
                              filter,
                              Equal(groupPath, groupValue)
                           ),
                           subName + "/" + groupValue,
                           restGroupings.tail
                        )
                     }
                     runQuery(
                        filter,
                        subName + "/" + "!Razem",
                        Nil
                     )
                  }
               }

               for (firstLevelGroupValue <- firstLevelGroupings) {
                   if (firstLevelGroupValue.toString.isEmpty) {
                     println(s"Skipping some records because their ${firstLevelGroupPath.toString()} is empty")
                   } else {
                     updateMessage("Running " + qname + ", " + firstLevelGroupValue)
                     runQuery(
                       And(
                         Equal(firstLevelGroupPath, firstLevelGroupValue),
                         charakterFilter
                       ),
                       firstLevelGroupValue.toString, folderGroupings.tail
                     )
                   }
                  progress += 1
                  updateProgress(progress, workToDo)
                  if (isCancelled) throw new RuntimeException("Cancelled")
               }

               updateMessage("Running " + qname + ", ALL")
               runQuery(Truth, "!Razem", Nil)
               progress += 1
               updateProgress(progress, workToDo)
            }

            updateMessage("Done")
         }
      }
   }

}
