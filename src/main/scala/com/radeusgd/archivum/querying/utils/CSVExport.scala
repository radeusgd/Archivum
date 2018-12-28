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
         val header = results.head.columnNames
         printWriter.println(header.map(escape).mkString(sep))
         for (row <- results) {
            val line = row.cells.map(cellToString).map(escape).mkString(sep)
            printWriter.println(line)
         }
      }
   }

   def export(file: File, results: Seq[ResultRow]): Unit = {
      export(new PrintWriter(new FileWriter(file)), results)
   }
}
