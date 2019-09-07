package com.radeusgd.archivum.gui.scenes

import java.io.{OutputStream, PrintWriter, Writer}
import java.lang.reflect.Modifier
import java.nio.charset.Charset

import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import javafx.util.Callback
import org.controlsfx.control.textfield.{AutoCompletionBinding, TextFields}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Label, TextArea, TextField}
import scalafx.scene.layout.{Pane, VBox}

import scala.collection.JavaConverters._
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.util.control.NonFatal

class Repl(val repository: Repository, val parentScene: Scene) extends Scene {
   val results = new TextArea()
   results.editable = false
   results.setPrefWidth(800)
   results.setPrefHeight(800)

   private def print(line: String): Unit = {
      results.text = results.text.value + "\n" + line
   }

   val cmd = new TextField()
   cmd.setPrefWidth(800)
   cmd.onAction = _ => {
      val evaluated = eval.interpret(cmd.text.value)
      cmd.text = ""
      println(eval.allDefinedNames)
   }
   private val suggestionProvider: Callback[AutoCompletionBinding.ISuggestionRequest, java.util.Collection[String]] =
      (request: AutoCompletionBinding.ISuggestionRequest) => {
         val hint = request.getUserText
         if (hint.forall(_.isLetterOrDigit)) {
            eval.definedTerms.map(_.toString).filter(_.startsWith(hint)).asJavaCollection
         } else if (!hint.isEmpty && hint.dropRight(1).forall(p => p.isLetterOrDigit) && hint.last == '.'
            && eval.definedTerms.map(_.toString).contains(hint.dropRight(1))) {
            eval.valueOfTerm(hint.dropRight(1)).map(term => {
               val funs = term.getClass.getMethods.filter(m => Modifier.isPublic(m.getModifiers) && !m.getName.contains("$")).map(_.getName)
               funs.map(hint + _).toList.asJavaCollection
            }).getOrElse(Nil.asJavaCollection)
         } else {
            Nil.asJavaCollection
         }
      }

   TextFields.bindAutoCompletion(cmd, suggestionProvider)

   private val vbox = new VBox(15,
      utils.makeGoToButton("< Powrót", parentScene),
      new Label("Funkcja dla zaawansowanych użytkowników.\nPozwala na dowolne modyfikacje danych.\nPrzed użyciem zaleca się wykonanie kopii zapasowej."),
      results,
      cmd) {
      padding = Insets(10, 10, 10, 10)
   }

   content = vbox

   private val settings = new Settings
   settings.usejavacp.value = true
   settings.deprecation.value = true

   private val writer = new Writer {
      override def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
         results.text =
            results.text.value + cbuf.slice(off, off + len).mkString
      }

      override def flush(): Unit = {}

      override def close(): Unit = {}
   }

   val eval = new IMain(
      settings,
      new PrintWriter(writer)
   )

   eval.bind("repo", repository)
   eval.bind("Help", Help)
}

object Help {
   def listMethods(x: Any): Unit = {
      print(x.getClass.getMethods.filter(m => Modifier.isPublic(m.getModifiers)).map(_.getName).mkString("\n"))
   }
}