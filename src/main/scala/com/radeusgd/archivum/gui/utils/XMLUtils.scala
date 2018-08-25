package com.radeusgd.archivum.gui.utils

import java.util.regex.Pattern

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.languages.ViewLanguage
import spray.json.DeserializationException

import scala.collection.generic.FilterMonadic
import scala.xml.Node

object XMLUtils {
   def extractPath(xmlnode: Node): Either[LayoutParseError, List[String]] =
      (for {
         pathAttr <- xmlnode.attribute(ViewLanguage.BindingPath)
         path <- pathAttr.headOption.map(n => DMUtils.parsePath(n.text))
         if path.nonEmpty
      } yield path).toRight(LayoutParseError(xmlnode, "Path not specified"))

   def properChildren(xmlnode: Node): Seq[Node] =
      xmlnode.child.filter(_.label != "#PCDATA")

   /*
   A very basic preprocessor.
   finds lines like
   $name=something
   and substitutes all occurrences of {name} with the thing
    */
   def preprocessXMLText(text: String): String = {
      val lines = text.split('\n')
      val subsitute_defs = lines.filter(_.startsWith("$"))
      val substition = "\\$(\\w+)=(.*)".r
      case class Substitution(name: String, replaceWith: String)
      val substitutions = subsitute_defs.map {
         case substition(name, replaceWith) => Substitution(name, replaceWith)
         case line: String => throw DeserializationException("Wrong substitution format " + line)
      }
      substitutions.foldLeft(text)(
         (text: String, sub: Substitution) => text.replaceAll(Pattern.quote("{" + sub.name + "}"), sub.replaceWith)
      )
   }
}
