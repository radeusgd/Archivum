package com.radeusgd.archivum.querying.builtinqueries

import java.io.File

import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.ResultRow
import com.radeusgd.archivum.querying.utils.CSVExport
import javafx.concurrent.Task

abstract class BuiltinQuery {

   val queries: Map[String, Repository => Seq[ResultRow]]

   def prepareTask(resultPath: String, repo: Repository): Task[Unit] = new Task[Unit]() {
      override def call(): Unit = {
         val workToDo = queries.size
         updateMessage("Creating directories")
         new File(resultPath).mkdirs() // create parent directories
         updateProgress(0, workToDo)

         for (((qname, query), index) <- queries.zipWithIndex) {
            updateMessage("Running " + qname)
            val res = query(repo)
            CSVExport.export(new File(resultPath + qname + ".csv"), res)
            Thread.sleep(1000)
            updateProgress(index + 1, workToDo)
         }

         updateMessage("Done")
      }
   }
}
