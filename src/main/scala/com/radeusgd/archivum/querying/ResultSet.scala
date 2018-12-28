package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMStruct, DMValue}

case class ResultSet(rows: Seq[MultipleResultRow]) {
   def getResult: Seq[ResultRow] = rows.map(_.prefix)

   def filter(predicate: DMValue => Boolean): ResultSet =
      ResultSet(rows.map(_.filter(predicate)))

   /*
   See MultipleResultRow for description.
    */
   def unpackArray(path: String): ResultSet =
      ResultSet(rows.map(_.unpackArray(path)))


   def flatten(): ResultSet = {
      def unpackRow(row: MultipleResultRow): Seq[MultipleResultRow] =
         row.objects.map(obj => MultipleResultRow(row.prefix, Seq(obj)))
      ResultSet(rows.flatMap(unpackRow))
   }

   def flatMap(f: MultipleResultRow => Seq[MultipleResultRow]): ResultSet = {
      ResultSet(rows.flatMap(f))
   }

   /*
   Groups results contained in each of MultipleResultRows into separate, smaller MultipleResultRows.
   If columnName is specified, value that has been grouped by is
    */
   def groupBy[A](path: String, appendPrefix: Option[AppendPrefix], sortBy: DMValue => A)(implicit ord: Ordering[A]): ResultSet = {
      flatMap(_.groupBy(path, appendPrefix, sortBy))
   }

   def aggregate(aggregations: (String, Seq[DMValue] => DMValue)*): Seq[ResultRow] = {
      rows.map(_.aggregate(aggregations))
   }
}
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.Settings

object Helper {
   var diff: Int = 5
   val f: Int => Int = (x: Int) => x + diff
}

object Tmp {
   def main(args: Array[String]): Unit = {
      evaluate()
   }

   def evaluate(): Unit = {
      val clazz = prepareClass
      val settings = new Settings
      settings.usejavacp.value = true
      settings.deprecation.value = true

      println("Evaluating...")
      val eval = new IMain(settings)
      //val evaluated = eval.beSilentDuring(eval.interpret(clazz))
      eval.directBind("Helper", Helper)
      val evaluated = eval.interpret(clazz)
      val res = eval.valueOfTerm("res0").get.asInstanceOf[Int => Int]
      println(res)
      println(res(0))
      Helper.diff = 100
      println(res(0))
   }

   private def prepareClass: String = {
      /*s"""
         |val f = (x: Int) => x + 5
         |f
         |""".stripMargin*/
      s"""
         |Helper.f
         |""".stripMargin
   }
}