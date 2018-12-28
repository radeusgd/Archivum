package com.radeusgd.archivum.querying.utils

import java.io.{File, FileWriter, PrintWriter}

import com.radeusgd.archivum.querying.{ResultCell, ResultRow}

object CSVExport {

   def escape(str: String): String =
      "\"" + str.replace("\"", "\\\"") + "\""

   def cellToString(cell: ResultCell): String = {
      cell.value.toString // TODO may conider using StringBridges ?
   }

   private val sep: String = ","

   def export(printWriter: PrintWriter, results: Seq[ResultRow]): Unit = {
      if (results.nonEmpty) {
         val header = results.head.columnNames.reverse
         val headLine = header.map(escape).mkString(sep)
         printWriter.println(headLine)
         println(headLine)
         for (row <- results) {
            val line = row.cells.reverse.map(cellToString).map(escape).mkString(sep)
            printWriter.println(line)
            println(line)
         }
      }
   }

   def export(file: File, results: Seq[ResultRow]): Unit = {
      val pw = new PrintWriter(new FileWriter(file))
      export(pw, results)
      pw.close()
   }
}
