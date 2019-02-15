package com.radeusgd.archivum.querying.utils

import java.nio.file.{Files, Path, Paths}

import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.{CellHorizontalAlignment, CellVerticalAlignment}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.radeusgd.archivum.datamodel.{DMDate, DMInteger, DMStruct, DMValue}
import com.radeusgd.archivum.querying.{ListMap, NestedMap, NestedMapElement, ResultRow}

import scala.collection.Map

object XLSExport {

   private val percentageStyle = CellStyle(dataFormat = CellDataFormat("0.0"))
   private val integerStyle = CellStyle(dataFormat = CellDataFormat("0"))

   def makeCell(cellValue: DMValue): Cell = {
      cellValue match {
         case DMInteger(x) => Cell(x, style = integerStyle)
         case DMDate(d) => Cell(d)
         case struct @ DMStruct(mapping, _) =>
            val fraction: Option[(DMValue, DMValue)] = for {
               part <- mapping.get("part")
               whole <- mapping.get("whole")
            } yield (part, whole)

            val fracCell: Option[Cell] = fraction match {
               case Some((DMInteger(part), DMInteger(whole))) =>
                  if (whole != 0)
                     Some(Cell(100.0 * part / whole, style = percentageStyle))
                  else
                     Some(Cell(null))
               case _ => None
            }

            fracCell.getOrElse(Cell(struct.toString))
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

   private val headerStyle = CellStyle(
      font=Font(bold = true),
      horizontalAlignment = CellHorizontalAlignment.Center,
      verticalAlignment = CellVerticalAlignment.Center
   )

   private def emptyCells(width: Int): List[Cell] =
      List.fill(width)(Cell(null, style = headerStyle))

   private def makeHeader(name: String, width: Int, offset: Int): HeaderAccumulator = {
      HeaderAccumulator(
         rows = (Cell(name, style = headerStyle) :: emptyCells(width - 1)) :: Nil,
         mergedRegions = Nil,
         topMergedRegions = CellRange(0 -> 0, offset -> (offset + width - 1)) :: Nil,
         width = width
      )
   }

   private def moveRangesDown(ranges: List[CellRange], amount: Int = 1): List[CellRange] =
      ranges.map {
         case CellRange((r1, r2), c) => CellRange((r1 + amount, r2 + amount), c)
      }

   private def extendHeader(name: String, header: HeaderAccumulator, offset: Int): HeaderAccumulator = {
      HeaderAccumulator(
         rows = (Cell(name, style = headerStyle) :: emptyCells(header.width - 1)) :: header.rows,
         mergedRegions = moveRangesDown(header.topMergedRegions ++ header.mergedRegions),
         topMergedRegions = CellRange(0 -> 0, offset -> (offset + header.width - 1)) :: Nil,
         width = header.width
      )
   }

   private def padRowsToHeight(height: Int)(header: HeaderAccumulator): HeaderAccumulator = {
      val padding = height - header.height
      header.copy(
         rows = header.rows ++ List.fill(padding)(emptyCells(header.width)),
         mergedRegions = moveRangesDown(header.mergedRegions, padding),
         topMergedRegions = header.topMergedRegions.map({
            case CellRange((r1, r2), c) => CellRange((r1, r2 + padding), c)
         })
      )
   }

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
         topMergedRegions = hs.flatMap(_.topMergedRegions),
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
                  extendHeader(key, makeHeader(m, noffset), noffset)
            }
            (noffset + ha.width, ha :: l)
      })
      val haccs = rhaccs.reverse
      if (haccs.isEmpty) {
         return HeaderAccumulator(Nil, Nil, Nil, 0)
      }

      val maxHeight = haccs.map(_.height).max

      val padded = haccs.map(padRowsToHeight(maxHeight))

      mergeHeaders(padded)
   }

   private def makeHeader(results: Seq[ResultRow]): (List[Row], List[CellRange]) = {
      val headerStructures: Seq[NestedMap[String, Int]] = results.map(_.map(_ => 1))
      val headerStruct1 = headerStructures.head
      val structuresIsomorphic = headerStructures.forall(_ == headerStruct1)
      if (!structuresIsomorphic) {
         println(headerStructures.map(_.toString).mkString("\n"))
         throw new RuntimeException("Row headers are not the same for all rows!")
      }

      val ha = makeHeader(headerStruct1, 0)
      (ha.rows.map(Row(_)), ha.mergedRegions ++ ha.topMergedRegions)
   }

   def filterEmptyMerges(l: List[CellRange]): List[CellRange] =
      l.filter({
         case CellRange((r1, r2), (c1, c2)) => r1 < r2 || c1 < c2
      })

   def makeSheet(results: Seq[ResultRow]): Sheet = {
      if (results.nonEmpty) {
         val (headerRows, headerMerges) = makeHeader(results)
         val rows = results.map((row: ResultRow) =>
            Row(row.flatten.map(makeCell))
         )

         val sheet = Sheet(
            name = "Results", // TODO better naming?
            rows = headerRows ++ rows,
            mergedRegions = filterEmptyMerges(headerMerges),
            repeatingColumns = ColumnRange("A" -> "A"),
            repeatingRows = RowRange(1 -> headerRows.length),
            paneAction = FreezePane(1, headerRows.length)
         )

         sheet
      } else {
         Sheet("Empty resultset")
      }
   }

   def export(fileName: String, results: Seq[ResultRow]): Unit = {
      Files.createDirectories(Paths.get(fileName).getParent)
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
