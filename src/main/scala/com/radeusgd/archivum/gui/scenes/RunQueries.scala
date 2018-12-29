package com.radeusgd.archivum.gui.scenes

import java.io.{File, PrintWriter}

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.builtinqueries.{BuiltinQuery, Chrzty}
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import scalafx.Includes.handle
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}

class RunQueries(val repository: Repository, parentScene: Scene) extends Scene {

   val builtin = Seq(new Chrzty)
   val builtinChooser =
      new ComboBox[BuiltinQuery](builtin)

   val progressBar = new ProgressBar()
   val statusText = new Label()

   content = new VBox(
      utils.makeGoToButton("< Back", parentScene),
      new HBox(
         new Label("Builtin querysets"),
         builtinChooser,
         new Button("Run") {
            onAction = handle {
               val query = builtinChooser.value.value
               val task = query.prepareTask("queryres/", repository)

               progressBar.progress.unbind()
               progressBar.progress.bind(task.progressProperty())
               statusText.text.unbind()
               statusText.text.bind(task.messageProperty())

               task.setOnFailed((event: WorkerStateEvent) => {
                  utils.reportException("Task failed", task.getException)
                  statusText.text.unbind()
                  statusText.text = "Failed: " + task.getException.toString
               })

               val t = new Thread(task)
               t.start()
            }
         }
      ),
      new HBox(progressBar, statusText)
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

