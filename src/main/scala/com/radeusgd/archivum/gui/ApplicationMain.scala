package com.radeusgd.archivum.gui

import java.nio.charset.Charset

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

   def setPrimaryStageVisibility(visible: Boolean): Unit =
      if (visible) stage.show()
      else stage.hide()

   Thread.setDefaultUncaughtExceptionHandler((th, ex) => utils.reportException("There was an unhandled error", ex))

   private var longRunningTasks: mutable.MutableList[Task[_]] = mutable.MutableList.empty
   def registerLongRunningTask(task: Task[_]): Unit =
      longRunningTasks += task

   override def stopApp(): Unit = {
      longRunningTasks.foreach(_.cancel()) // TODO make sure this works ok if task was finished already
   }

   if (Charset.defaultCharset().name() != "UTF-8") {
      if(!utils.ask(
         "Baza danych uruchomiona ze złym kodowaniem: " + Charset.defaultCharset().name(),
         "Otwarcie bazy w ten sposób może doprowadzić do jej uszkodzenia, czy napewno chcesz kontynuować? (nie zalecane)\n" +
            "Proszę uruchomić bazę danych z opcją -Dfile.encoding=UTF-8 lub za pomocą dostarczonego skrótu."
      )) {
         new RuntimeException("Zamykanie bazy z powodu złego kodowania")
      }
   }
}
