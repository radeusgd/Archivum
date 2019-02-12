package com.radeusgd.archivum.querying.utils

import java.nio.file.{Files, Path}

import com.norbitltd.spoiwo.model.{Cell, Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.radeusgd.archivum.datamodel.{DMDate, DMInteger, DMValue}
import com.radeusgd.archivum.querying.{ResultCell, ResultRow}

object XLSExport {

   def makeCell(cell: ResultCell): Cell = {
      cell.value match {
         case DMInteger(x) => Cell(x)
         case DMDate(d) => Cell(d)
         case other: DMValue => Cell(other.toString)
      }
   }

   private val sep: String = ","

   def export(fileName: String, results: Seq[ResultRow]): Unit = {
      if (results.nonEmpty) {

         val headerCols = results.head.columnNames.reverse
         val header = Row(headerCols.map(Cell(_)))
         val rows = results.map((row: ResultRow) =>
            Row(row.cells.reverse.map(makeCell))
         ).toList

         val sheet = Sheet(
            name = "Results", // TODO better naming?
            rows = header :: rows
         )
         sheet.saveAsXlsx(fileName)
      } else {
         println("Warning! Empty result set, export will not take effect.")
      }
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
            export(path.resolve(filename).toString, reverseRows(results))
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
