package com.radeusgd.archivum.gui.utils

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.languages.ViewLanguage

import scala.collection.generic.FilterMonadic
import scala.xml.Node

object XMLUtils {
   def extractPath(xmlnode: Node): Either[LayoutParseError, List[String]] =
      (for {
         pathAttr <- xmlnode.attribute(ViewLanguage.BindingPath)
         path <- pathAttr.headOption.map(n => DMUtils.parsePath(n.text))
         if path.nonEmpty
      } yield path).toRight(LayoutParseError("Path not specified"))

   def properChildren(xmlnode: Node): Seq[Node] =
      xmlnode.child.filter(_.label != "#PCDATA")
}
