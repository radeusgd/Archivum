package com.radeusgd.archivum.gui.scenes

import java.io.File

import com.radeusgd.archivum.gui.{ApplicationMain, utils}
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.builtinqueries.{BuiltinQuery, Małżeństwa, Urodzenia, Zgony}
import javafx.concurrent.WorkerStateEvent
import scalafx.Includes.handle
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}

class RunQueries(val repository: Repository, parentScene: Scene) extends Scene {

   case class QueryRecipe(recipe: (Int, Seq[String], Option[String]) => BuiltinQuery, name: String) {
      override def toString: String = name
   }

   val builtin: Seq[QueryRecipe] = Seq(
      QueryRecipe(new Urodzenia(_, _, _), "Urodzenia"),
      QueryRecipe(new Małżeństwa(_, _, _), "Małżeństwa"),
      QueryRecipe(new Zgony(_, _, _), "Zgony")
   )

   val builtinChooser =
      new ComboBox[QueryRecipe](builtin)
   builtin.find(_.name == repository.model.name).foreach(builtinChooser.value = _)

   val progressBar = new ProgressBar()
   progressBar.setPrefWidth(500)
   val statusText = new Label()

   val periodChooser: TextField = new TextField()
   periodChooser.text = "5"
   periodChooser.prefWidth = 50
   periodChooser.text.onChange((_, _, _) => {
      val num = raw"\d*".r
      val notnum = raw"[^\d]".r
      periodChooser.text match {
         case num() => ()
         case _ => {
            periodChooser.text = notnum.replaceAllIn(periodChooser.text.value, "")
         }
      }
   })

   class FolderGrouping(val value: Seq[String]) {
      override def toString: String = value.mkString(", ")
   }

   object FolderGrouping {
      def apply(args: String*): FolderGrouping = new FolderGrouping(args)
   }

   val folderGroupings = Seq(
      FolderGrouping("Parafia", "Miejscowość"),
      FolderGrouping("Powiat", "Miejscowość"),
      FolderGrouping("Dekanat", "Parafia", "Miejscowość"),
      FolderGrouping("Diecezja", "Dekanat", "Parafia", "Miejscowość")
   )

   val groupingChooser = new ComboBox[FolderGrouping](folderGroupings)
   groupingChooser.value = folderGroupings.head


   private val ALLcharkatery = "WSZYSTKIE"
   val charaktery = Seq(
      ALLcharkatery,
      "miasto",
      "miasteczko",
      "przedmieście",
      "wieś",
      "przysiółek"
   )
   val charakterChooser = new ComboBox[String](charaktery)
   charakterChooser.value = charaktery.head

   val startTaskButton: Button = new Button("Uruchom") {
      onAction = handle {

         val period: Int = periodChooser.text.value.toInt
         val grouping: Seq[String] = groupingChooser.value.value.value
         val charakter: Option[String] = charakterChooser.value.value match {
            case ALLcharkatery => None
            case other: String => Some(other)
         }

         val query = builtinChooser.value.value.recipe(period, grouping, charakter)

         val defaultPath = "Wyniki kwerend/" + repository.model.name + "/"
         val path = utils.chooseSaveDirectory("Wyniki kwerend", new File(defaultPath))
            .map(_.toPath.toString).getOrElse(defaultPath)
         val task = query.prepareTask(path, repository)

         progressBar.progress.unbind()
         progressBar.progress.bind(task.progressProperty())
         statusText.text.unbind()
         statusText.text.bind(task.messageProperty())

         task.setOnFailed((event: WorkerStateEvent) => {
            utils.reportException("Task failed", task.getException)
            statusText.text.unbind()
            statusText.text = "Failed: " + task.getException.toString
         })

         this.delegate.disableProperty().bind(task.runningProperty())

         ApplicationMain.registerLongRunningTask(task)
         val t = new Thread(task)
         t.start()
      }
   }

   content = new VBox(
      utils.makeGoToButton("< Powrót", parentScene),
      new HBox(
         new Label("Okres co "),
         periodChooser,
         new Label(" lat")
      ),
      new HBox(
         new Label("Grupuj według: "),
         groupingChooser
      ),
      new HBox(
         new Label("Charakter miejscowości: "),
         charakterChooser
      ),
      new HBox(
         new Label("Wbudowane zestawy kwerend: "),
         builtinChooser,
         startTaskButton
      ),
      progressBar,
      statusText
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

