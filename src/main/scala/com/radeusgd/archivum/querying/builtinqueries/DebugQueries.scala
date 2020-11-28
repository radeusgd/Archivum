package com.radeusgd.archivum.querying.builtinqueries

import com.radeusgd.archivum.gui.scenes.RunQueries
import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Database
import javafx.concurrent.{Task, WorkerStateEvent}
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene

object DebugQueries extends JFXApp {
  private val db = Database.open()
  private val repo = db.openRepository("Chrzty").get

  private var currentTask: Option[Task[Unit]] = None

  Platform.runLater {
    val query = new Urodzenia(5)
    val task: Task[Unit] =
      query.prepareTask(java.nio.file.Path.of("queryres/"), repo)

    currentTask = Some(task)
    utils.runTaskWithProgress("Debug queries", task)
  }

  stage = new JFXApp.PrimaryStage {
    title = "Archivum"
    width = 800
    height = 600
    scene = new Scene
  }

  override def stopApp(): Unit = {
    currentTask.foreach(_.cancel())
  }
}
