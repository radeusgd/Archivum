package com.radeusgd.archivum.gui

import com.radeusgd.archivum.gui.scenes.MainMenu
import javafx.concurrent.Task
import scalafx.application.JFXApp
import scalafx.scene.Scene

import scala.collection.mutable

object ApplicationMain extends JFXApp {

   def switchScene(scene: Scene): Unit = {
      stage.scene = scene
   }

   private val defaultWidth = 600
   private val defaultHeight = 450

   stage = new JFXApp.PrimaryStage {
      title = "Archivum"
      width = defaultWidth
      height = defaultHeight
      scene = MainMenu.instance
   }

   Thread.setDefaultUncaughtExceptionHandler((th, ex) => utils.reportException("There was an unhandled error", ex))

   private var longRunningTasks: mutable.MutableList[Task[_]] = mutable.MutableList.empty
   def registerLongRunningTask(task: Task[_]): Unit =
      longRunningTasks += task

   override def stopApp(): Unit = {
      longRunningTasks.foreach(_.cancel()) // TODO make sure this works ok if task was finished already
   }
}
