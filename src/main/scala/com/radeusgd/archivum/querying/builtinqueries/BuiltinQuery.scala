package com.radeusgd.archivum.querying.builtinqueries

import java.io.File
import java.nio.file.{Path, Paths}

import com.radeusgd.archivum.datamodel.{DMUtils, DMValue}
import com.radeusgd.archivum.persistence.{Equal, Repository, SearchCriteria, Truth}
import com.radeusgd.archivum.querying._
import com.radeusgd.archivum.querying.utils.CSVExport
import javafx.concurrent.Task

//noinspection ScalaStyle
abstract class BuiltinQuery(years: Int, folderGroupings: Seq[String]) {

   val queries: Map[String, ResultSet => Seq[ResultRow]]

   def prepareTask(resultPath: String, repo: Repository): Task[Unit] = new Task[Unit]() {
      override def call(): Unit = {
         if (folderGroupings.isEmpty) {
            val workToDo = queries.size

            updateProgress(0, workToDo)

            for (((qname, query), index) <- queries.zipWithIndex) {
               updateMessage("Running " + qname)
               val all = repo.fetchAllGrouped()
               val res = query(all)
               CSVExport.export(new File(resultPath + qname + ".csv"), res)
               updateProgress(index + 1, workToDo)
            }

            updateMessage("Done")
         } else {
            updateProgress(0, 1)
            val firstLevelGroupPath = DMUtils.parsePath(folderGroupings.head)
            val firstLevelGroupings = repo.getAllDistinctValues(firstLevelGroupPath)
            val flgLen = firstLevelGroupings.length
            val workToDo = queries.size * (flgLen + 1) + 1
            var progress = 1
            updateProgress(progress, workToDo)
            println("Will run for " + firstLevelGroupings)

            for ((qname, query) <- queries) {

               def runQuery(filter: SearchCriteria, subName: String): Unit = {
                  val all = repo.fetchAllGrouped(
                     filter,
                     folderGroupings.tail.map(path => GroupByWithSummary(path)):_*
                  )
                  val res = query(all)
                  CSVExport.exportToSubFolders(Paths.get(resultPath).resolve(subName), qname + ".csv", folderGroupings.tail.length, res)
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
