package com.radeusgd.archivum.querying.builtinqueries

import java.nio.file.{Path, Paths}

import com.radeusgd.archivum.datamodel.{DMUtils, DMValue}
import com.radeusgd.archivum.persistence.{Equal, Repository, SearchCriteria, Truth}
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.querying.utils.XLSExport
import javafx.concurrent.Task

sealed abstract class YearGrouping
object DataChrztu extends YearGrouping
object DataUrodzenia extends YearGrouping
object NoYearGrouping extends YearGrouping

case class Query(yearGrouping: YearGrouping, realization: ResultSet => Seq[ResultRow]) {
   def withFilter(predicate: DMValue => Boolean): Query =
      Query(yearGrouping, (rs: ResultSet) => realization(rs.filter(predicate)))
}

//noinspection ScalaStyle
abstract class BuiltinQuery(years: Int, folderGroupings: Seq[String]) {

   val groupedQueries: Map[String, Query]
   val manualQueries: Map[String, ResultSet => Seq[ResultRow]]

   private def prepareYearGrouping(yearGrouping: YearGrouping): Seq[Grouping] = yearGrouping match {
      case DataChrztu => GroupByYears("Data chrztu", years, CustomAppendColumn("Rok")) :: Nil
      case DataUrodzenia => GroupByYears("Data urodzenia", years, CustomAppendColumn("Rok")) :: Nil
      case NoYearGrouping => Nil
   }

   private val fileExt: String = ".xlsx"

   def prepareTask(resultPath: String, repo: Repository): Task[Unit] = new Task[Unit]() {
      private def runManualQueries(workToDo: Int): Unit = {
         for (((qname, query), index) <- manualQueries.zipWithIndex) {
            val all = repo.fetchAll()
            val res = query(all)

            XLSExport.export(resultPath + qname + fileExt, res)
            println(s"Query $qname written ${res.length} rows in total")
            updateProgress(index + 1, workToDo)
         }
      }

      override def call(): Unit = {
         if (folderGroupings.isEmpty) {
            val workToDo = groupedQueries.size + manualQueries.size

            updateProgress(0, workToDo)

            runManualQueries(workToDo)

            for (((qname, Query(yg, query)), index) <- groupedQueries.zipWithIndex) {
               updateMessage("Running " + qname)
               val all = repo.fetchAllGrouped(prepareYearGrouping(yg) : _*)
               val res = query(all)
               XLSExport.export(resultPath + qname + fileExt, res)
               println(s"Query $qname written ${res.length} rows in total")
               updateProgress(manualQueries.size + index + 1, workToDo)
            }

            updateMessage("Done")
         } else {
            updateProgress(0, 1)
            val firstLevelGroupPath = DMUtils.parsePath(folderGroupings.head)
            val firstLevelGroupings = repo.getAllDistinctValues(firstLevelGroupPath)
            val flgLen = firstLevelGroupings.length
            val workToDo = groupedQueries.size * (flgLen + 1) + 1 + manualQueries.size
            updateProgress(1, workToDo)

            runManualQueries(workToDo)

            var progress = manualQueries.size + 1
            updateProgress(progress, workToDo)
            println("Will run for " + firstLevelGroupings)

            for ((qname, Query(yg, query)) <- groupedQueries) {

               def runQuery(filter: SearchCriteria, subName: String): Unit = {
                  val groupings =
                     folderGroupings.tail.map(path => GroupByWithSummary(path)) ++ prepareYearGrouping(yg)
                  val all = repo.fetchAllGrouped(
                     filter,
                     groupings:_*
                  )
                  val res = query(all)
                  XLSExport.exportToSubFolders(Paths.get(resultPath).resolve(subName), qname + fileExt, folderGroupings.tail.length, res)
                  println(s"Query $qname @ $subName written ${res.length} rows in total")
               }

               for (firstLevelGroupValue <- firstLevelGroupings) {
                  updateMessage("Running " + qname + ", " + firstLevelGroupValue)
                  runQuery(Equal(firstLevelGroupPath, firstLevelGroupValue), firstLevelGroupValue.toString)
                  progress += 1
                  updateProgress(progress, workToDo)
               }

               updateMessage("Running " + qname + ", ALL")
               runQuery(Truth, "ALL")
               progress += 1
               updateProgress(progress, workToDo)
            }

            updateMessage("Done")
         }
      }
   }

}
