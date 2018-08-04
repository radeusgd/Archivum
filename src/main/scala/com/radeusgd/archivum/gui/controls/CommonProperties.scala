package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.layout.{LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage

case class CommonProperties(
                           label: String,
                           width: Option[Int],
                           sticky: Boolean,
                           default: Option[String]
                           )
// TODO rows

object CommonProperties {
   private def getAttr(xmlnode: xml.Node, name: String): Option[String] =
      xmlnode.attribute(name).map(_.text)

   def parseXML(xmlnode: xml.Node): Either[LayoutParseError, CommonProperties] = {
      val optLabel: Option[String] = getAttr(xmlnode, ViewLanguage.Label)
      val width: Option[Int] = getAttr(xmlnode, ViewLanguage.Width).map(_.toInt)
      val default: Option[String] = getAttr(xmlnode, ViewLanguage.Default)
      val sticky: Either[LayoutParseError, Boolean] =
         getAttr(xmlnode, ViewLanguage.Sticky) match {
            case Some(ViewLanguage.True) => Right(true)
            case Some(ViewLanguage.False) => Right(false)
            case None => Right(false)
            case Some(_) => Left(LayoutParseError("Invalid value for field 'sticky'"))
         }

      for {
         path <- XMLUtils.extractPath(xmlnode)
         label = optLabel.getOrElse(path.last)
         sticky <- sticky
      } yield CommonProperties(
         label,
         width,
         sticky,
         default
      )
   }
}