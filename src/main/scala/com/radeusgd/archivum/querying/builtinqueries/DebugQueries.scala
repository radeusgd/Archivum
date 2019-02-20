package com.radeusgd.archivum.querying.builtinqueries

import com.radeusgd.archivum.gui.scenes.RunQueries
import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Database
import javafx.concurrent.{Task, WorkerStateEvent}
import scalafx.application.{JFXApp, Platform}

object DebugQueries extends JFXApp {
   private val db = Database.open()
   private val repo = db.openRepository("Chrzty").get

   private val qscene = new RunQueries(repo, null)
   private var currentTask: Option[Task[Unit]] = None

   Platform.runLater {
      val query = new Urodzenia(5)
      val task: Task[Unit] = query.prepareTask("queryres/", repo)

      val progressBar = qscene.progressBar
      val statusText = qscene.statusText

      progressBar.progress.unbind()
      progressBar.progress.bind(task.progressProperty())
      statusText.text.unbind()
      statusText.text.bind(task.messageProperty())

      task.setOnFailed((event: WorkerStateEvent) => {
         utils.reportException("Task failed", task.getException)
         statusText.text.unbind()
         statusText.text = "Failed: " + task.getException.toString
      })

      currentTask = Some(task)
      val t = new Thread(task)
      t.start()
   }

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = 800
      height = 600
      scene = qscene
   }

   override def stopApp(): Unit = {
      currentTask.foreach(_.cancel())
   }
}
