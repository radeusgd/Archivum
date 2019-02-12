package com.radeusgd.archivum.querying.utils

import java.nio.file.{Files, Path}

import com.norbitltd.spoiwo.model.{Cell, CellRange, Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.radeusgd.archivum.datamodel.{DMDate, DMInteger, DMValue}
import com.radeusgd.archivum.querying.{ListMap, NestedMap, NestedMapElement, ResultRow}

object XLSExport {

   def makeCell(cellValue: DMValue): Cell = {
      cellValue match {
         case DMInteger(x) => Cell(x)
         case DMDate(d) => Cell(d)
         case other: DMValue => Cell(other.toString)
      }
   }

   private val sep: String = ","

   private case class HeaderAccumulator(
      rows: List[List[Cell]],
      mergedRegions: List[CellRange],
      topMergedRegions: List[CellRange],
      width: Int) {
      def height: Int = rows.length
   }

   private def emptyCells(width: Int): List[Cell] =
      List.fill(width)(Cell(null))

   private def makeHeader(name: String, width: Int, offset: Int): HeaderAccumulator = {
      HeaderAccumulator(
         rows = (Cell(name) :: emptyCells(width - 1)) :: Nil,
         mergedRegions = Nil,
         topMergedRegions = CellRange((0, 0), (0, 0)) :: Nil,
         width = width
      )
   }

   private def extendHeader(name: String, header: HeaderAccumulator, offset: Int): HeaderAccumulator =
      HeaderAccumulator(
         rows = (Cell(name) :: emptyCells(header.width)) :: header.rows,
         mergedRegions = Nil, // TODO
         topMergedRegions = Nil,
         width = header.width
      )

   private def padRowsToHeight(height: Int)(header: HeaderAccumulator): HeaderAccumulator =
      header.copy(
         rows = header.rows ++ List.fill(height - header.height)(emptyCells(header.width))
      )

   private def mergeRows(rows: List[List[List[Cell]]]): List[List[Cell]] = {
      if (rows.head.isEmpty) {
         Nil
      } else {
         val heads: List[List[Cell]] = rows.map(_.head)
         val mergedHeads: List[Cell] = heads.flatten

         val tails = rows.map(_.tail)
         mergedHeads :: mergeRows(tails)
      }
   }

   private def mergeHeaders(hs: List[HeaderAccumulator]): HeaderAccumulator = {
      HeaderAccumulator(
         rows = mergeRows(hs.map(_.rows)),
         mergedRegions = hs.flatMap(_.mergedRegions),
         topMergedRegions = Nil, // TODO
         width = hs.map(_.width).sum
      )
   }

   private def makeHeader(subtree: NestedMap[String, Int], offset: Int): HeaderAccumulator = {
      val (_, rhaccs) = subtree.mapping.entries.foldLeft[(Int, List[HeaderAccumulator])]((offset, Nil))({
         case ((noffset, l), (key, elem)) =>
            val ha = elem match {
               case NestedMapElement(value) =>
                  makeHeader(key, value, noffset)
               case m: NestedMap[String, Int] =>
                  makeHeader(m, noffset)
            }
            (noffset + ha.width, ha :: l)
      })
      val haccs = rhaccs.reverse

      val maxHeight = haccs.map(_.height).max

      val padded = haccs.map(padRowsToHeight(maxHeight))

      mergeHeaders(padded)
   }

   private def makeHeader(resultRow: ResultRow): (List[Row], List[CellRange]) = {
      val ha = makeHeader(resultRow.map(_ => 1), 0)
      (ha.rows.map(Row(_)), ha.mergedRegions ++ ha.topMergedRegions)
   }

   def makeSheet(results: Seq[ResultRow]): Sheet = {
      if (results.nonEmpty) {
         val (headerRows, headerMerges) = makeHeader(results.head)
         val rows = results.map((row: ResultRow) =>
            Row(row.flatten.map(makeCell))
         )

         val sheet = Sheet(
            name = "Results", // TODO better naming?
            rows = headerRows ++ rows,
            mergedRegions = headerMerges
         )

         sheet
      } else {
         Sheet("Empty resultset")
      }
   }

   def export(fileName: String, results: Seq[ResultRow]): Unit = {
      makeSheet(results).saveAsXlsx(fileName)
   }

   def exportToSubFolders(path: Path, filename: String, columnsToFolders: Int, results: Seq[ResultRow]): Unit = {

      def cutFirstCell(row: ResultRow): (String, ResultRow) = {
         val entries = row.mapping.entries
         (
            entries.head._2.asInstanceOf[NestedMapElement[String, DMValue]].value.toString, // TODO maybe add more explicit error
            NestedMap(ListMap.fromList(entries.tail))
         )
      }

      def exportHelper(path: Path, columns: Int, results: Seq[ResultRow]): Unit = {
         if (columns == 0) {
            Files.createDirectories(path)
            export(path.resolve(filename).toString, results)
         }
         else {
            val cut = results.map(cutFirstCell)
            val rowsMap: Map[String, Seq[ResultRow]] = cut.groupBy(_._1).mapValues(_.map(_._2))
            rowsMap.foreach({ case (name, rows) =>
               exportHelper(path.resolve(name), columns - 1, rows)
            })
         }
      }

      exportHelper(path, columnsToFolders, results)
   }
}
