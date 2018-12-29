package com.radeusgd.archivum.querying.utils

import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.{Files, Path}

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
         for (row <- results) {
            val line = row.cells.reverse.map(cellToString).map(escape).mkString(sep)
            printWriter.println(line)
         }
      }
   }

   def export(file: File, results: Seq[ResultRow]): Unit = {
      val pw = new PrintWriter(new FileWriter(file))
      export(pw, results)
      pw.close()
   }

   def exportToSubFolders(path: Path, filename: String, columnsToFolders: Int, results: Seq[ResultRow]): Unit = {
      def reverseRows(rows: Seq[ResultRow]): Seq[ResultRow] =
         rows.map(row => ResultRow(row.cells.reverse))

      def cutFirstCell(reversed: Seq[ResultRow]): Seq[(String, ResultRow)] =
         reversed.map(row =>
            (row.cells.head.value.toString, ResultRow(row.cells.tail))
         )

      def exportHelper(path: Path, columns: Int, results: Seq[ResultRow]): Unit = {
         if (columns == 0) {
            Files.createDirectories(path)
            export(new File(path.resolve(filename).toUri), reverseRows(results))
         }
         else {
            val cut = cutFirstCell(results)
            val rowsMap: Map[String, Seq[ResultRow]] = cut.groupBy(_._1).mapValues(_.map(_._2))
            rowsMap.foreach({ case (name, rows) =>
               exportHelper(path.resolve(name), columns - 1, rows)
            })
         }
      }

      exportHelper(path, columnsToFolders, reverseRows(results))
   }
}
