package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.LayoutDefaults
import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node
import scalafx.geometry.Pos
import scalafx.scene.control.Label

object LabelFactory extends LayoutFactory {
   private val defaultLabelFontSize = 20
   override def fromXML(xmlnode: Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout] = {
      val size: Int = xmlnode.attribute(ViewLanguage.FontSize)
         .map(_.text.toInt).getOrElse(defaultLabelFontSize)
      val text: String = xmlnode.text
      val label = new Label(text) {
         minWidth = LayoutDefaults.defaultFieldNameWidth
         alignment = Pos.CenterRight
         font = scalafx.scene.text.Font(font.name, size)
      }
      Right(ParsedLayout(label, Nil))
   }

   override val nodeType: String = ViewLanguage.Label
}