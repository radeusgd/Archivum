package com.radeusgd.archivum.gui.scenes

import java.io.{PrintWriter, Writer}

import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.querying.{DAOGenerator, Repl}
import scalafx.scene.control.{TextArea, TextField}
import scalafx.Includes._
import scalafx.scene.layout.VBox

class ReplInterface(repository: Repository) extends VBox {


   val prelude: String = "import com.radeusgd.archivum.datamodel.DMStruct\n" + DAOGenerator.makeDAO(repository.model)

   private val output = new TextArea() {
      prefWidth = 800
      prefHeight = 300
   }

   def appendLine(str: String): Unit = {
      output.appendText(str)
   }

   val repl: Repl = new Repl(Some(new PrintWriter(new Writer {
      override def write(chars: Array[Char], off: Int, len: Int): Unit = {
         appendLine(chars.slice(off, off + len).mkString)
      }

      override def flush(): Unit = ()

      override def close(): Unit = appendLine("EOF.")
   })))

   repl.bind("repo", repository)
   repl.evaluate(prelude)
   def modelClassName: String = {
      val n = repository.model.name
      n.head.toUpper + n.tail
   }
   repl.evaluate(s"def fetch(id: Long): $modelClassName = new $modelClassName (repo.fetchRecord(id).get)", true)
   //output.text = ""

   private val input = new TextField() {
      onAction = handle {
         val line = text.value
         appendLine("> " + line + "\n")
         text = ""
         repl.evaluate(line)
      }
   }

   children = Seq(
      new TextArea("// Prelude for script development\n" + prelude) {
         prefWidth = 800
         prefHeight = 300
      },
      output,
      input
   )
}
