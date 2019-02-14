package com.radeusgd.archivum.gui.scenes

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.builtinqueries.{BuiltinQuery, Chrzty}
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
      QueryRecipe(new Chrzty(_, _, _), "Chrzty")
   )

   val builtinChooser =
      new ComboBox[QueryRecipe](builtin)
   builtin.find(_.name == repository.model.name).foreach(builtinChooser.value = _)

   val progressBar = new ProgressBar()
   progressBar.setPrefWidth(500)
   val statusText = new Label()

   val periodChooser: TextField = new TextField()
   periodChooser.text = "5"
   periodChooser.text.onChange((a, b, c) => {
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

   content = new VBox(
      utils.makeGoToButton("< Back", parentScene),
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
         new Label("Builtin querysets"),
         builtinChooser,
         new Button("Run") {
            onAction = handle {

               val period: Int = periodChooser.text.value.toInt
               val grouping: Seq[String] = groupingChooser.value.value.value
               val charakter: Option[String] = charakterChooser.value.value match {
                  case ALLcharkatery => None
                  case other: String => Some(other)
               }

               val query = builtinChooser.value.value.recipe(period, grouping, charakter)
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
      progressBar,
      statusText
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

