package com.radeusgd.archivum.search

import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.AST.Expression
import com.radeusgd.archivum.languages.SimpleExpressionGrammar
import scalafx.scene.Node
import scalafx.scene.layout.{HBox, VBox}
import com.radeusgd.archivum.utils.BetterTuples._
import org.parboiled2.{ErrorFormatter, ParseError}

import scala.util.Try
import scala.xml.XML

case class SearchDefinition(criteriaNode: Node, conditions: Seq[ConditionGiver], columns: Seq[ResultColumn])

object SearchDefinition {
   def parseXML(text: String): SearchDefinition =
      parseXML(XML.loadString(XMLUtils.preprocessXMLText(text)))

   def parseXML(root: xml.Node): SearchDefinition = {
      val children: Map[String, xml.Node] = XMLUtils.properChildren(root).map(n => (n.label, n)).toMap
      val (node, conditions) = parseCriteriaFX(children("criteria"))

      val resultColumns = parseResults(children("results"))

      SearchDefinition(node, conditions, resultColumns)
   }

   private def parseResults(root: xml.Node): Seq[ResultColumn] =
      XMLUtils.properChildren(root).map(parseResultColumn)

   private def parseResultColumn(node: xml.Node): ResultColumn = {
      val name = node.attribute("name").map(_.text).getOrElse(throw new RuntimeException("Name of result column missing"))
      val expr: String = node.text
      val grammar = new SimpleExpressionGrammar(expr)
      val result: Try[Expression] = grammar.Statement.run()
      val parsedExpr: Expression = result.recoverWith {
         case pe: ParseError =>
            val err = grammar.formatError(pe, new ErrorFormatter(showTraces = false))
            println(err)
            com.radeusgd.archivum.gui.utils.showError("Parse error in " + expr, err)
            throw pe
      }.get
      val evaluator = new com.radeusgd.archivum.querying.Expression(parsedExpr)

      ResultColumn(
         name,
         d => {
            try {
               evaluator.evaluate(d).toString
            } catch {
               case e: Exception =>
                  println("Error computing " + parsedExpr)
                  e.printStackTrace()
                  "!Błąd obliczania wyrażenia!"
            }
         }
      )
   }

   private def parseAggregate(constructor: Seq[Node] => Node, nodes: Seq[xml.Node]): (Node, Seq[ConditionGiver]) = {
      val children = nodes.map(parseCriteriaNodeFX)
      (constructor(children.extractFirsts), children.extractSeconds.flatten)
   }

   private def wrapCriteriaNode(nodeWithCond: Node with ConditionGiver): (Node, Seq[ConditionGiver]) =
      (nodeWithCond, Seq(nodeWithCond))

   private def parseCriteriaFX(criteriaRoot: xml.Node): (Node, Seq[ConditionGiver]) =
      parseAggregate(new VBox(_:_*), XMLUtils.properChildren(criteriaRoot))

   private def getWidthForNode(node: xml.Node): Int =
      node.attribute("width").map(_.text.toInt).getOrElse(200)

   private def parseCriteriaNodeFX(node: xml.Node): (Node, Seq[ConditionGiver]) =
      node.label.toLowerCase match {
         case "vbox" => parseAggregate(new VBox(7, _:_*), XMLUtils.properChildren(node))
         case "hbox" => parseAggregate(new HBox(9, _:_*), XMLUtils.properChildren(node))
         case "field" =>
            val path: String = node.attribute("path").map(_.text).getOrElse(throw new RuntimeException("No path provided for exact search field"))
            val label: String = node.attribute("label").map(_.text).getOrElse(path)
            val width: Int = getWidthForNode(node)
            wrapCriteriaNode(new EqualConditionField(label, width, path))
         case "fulltext" =>
            val label: String = node.attribute("label").map(_.text).getOrElse(throw new RuntimeException("No label provided for fuzzy"))
            val width: Int = getWidthForNode(node)
            wrapCriteriaNode(new FulltextConditionField(label, width))
         case "year" =>
            val path: String = node.attribute("path").map(_.text).getOrElse(throw new RuntimeException("No path provided for exact search field"))
            val label: String = node.attribute("label").map(_.text).getOrElse(path)
            val width: Int = getWidthForNode(node)
            wrapCriteriaNode(new YearConditionField(label, width, path))
         case other: String => throw new RuntimeException(s"Unrecognized search field type in criteria $other")
      }
}
