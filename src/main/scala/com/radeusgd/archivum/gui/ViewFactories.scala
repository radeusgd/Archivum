package com.radeusgd.archivum.gui

import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node
import scalafx.geometry.Pos
import scalafx.scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox}

abstract class AggregateFactory(make: (Seq[scene.Node]) => scene.Node) extends ViewFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView): Either[ViewParseError, ParsedView] =
      if (xmlnode.attributes.nonEmpty) Left(ViewParseError("Unrecognized attributes"))
      else {
         val childrenResults = xmlnode.child filter (_.label != "#PCDATA") map (EditableView.parseViewTree(_, ev))
         val children: Either[ViewParseError, Seq[ParsedView]] = either_sequence(childrenResults)
         children map buildAggregate
      }

   private def buildAggregate(views: Seq[ParsedView]): ParsedView = {
      val nodes = views map (_.node)
      val boundeds = views flatMap (_.boundControls)
      ParsedView(make(nodes), boundeds)
   }

   // TODO use scalaz or Cats
   private def either_sequence[A, B](seq: Seq[Either[A, B]]): Either[A, Seq[B]] =
      seq.collectFirst({ case Left(l) => Left(l) }).getOrElse(
         Right(seq.collect({ case Right(r) => r }))
      )
}

object HBoxFactory extends AggregateFactory(new HBox(_: _*) { spacing = 5 }) {
   override val nodeType: String = ViewLanguage.Hbox
}

object VBoxFactory extends AggregateFactory(new VBox(_: _*) { spacing = 3 }) {
   override val nodeType: String = ViewLanguage.Vbox
}

object LabelFactory extends ViewFactory {
   override def fromXML(xmlnode: Node, ev: EditableView): Either[ViewParseError, ParsedView] = {
         val size: Int = xmlnode.attribute(ViewLanguage.FontSize).map(_.text.toInt).getOrElse(20) // TODO default font size
         val text: String = xmlnode.text
         val label = new Label(text) {
            minWidth = 100
            alignment = Pos.CenterRight
            font = scalafx.scene.text.Font(font.name, size)
         }
         Right(ParsedView(label, Nil))
      }

   override val nodeType: String = ViewLanguage.Label
}