package com.radeusgd.archivum.gui

import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node
import scalafx.scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, VBox}

abstract class AggregateFactory(make: (Seq[scene.Node]) => scene.Node) extends ViewFactory {
   override def fromXML(xmlnode: xml.Node): Either[ViewParseError, scene.Node] =
      if (xmlnode.attributes.nonEmpty) Left(ViewParseError("Unrecognized attributes"))
      else {
         val children = xmlnode.child filter (_.label != "#PCDATA") map EditableView.parseViewTree
         either_sequence(children) map make
      }

   // TODO use scalaz or Cats
   private def either_sequence[A, B](seq: Seq[Either[A, B]]): Either[A, Seq[B]] =
      seq.collectFirst({ case Left(l) => Left(l) }).getOrElse(
         Right(seq.collect({ case Right(r) => r }))
      )
}

object HBoxFactory extends AggregateFactory(new HBox(_: _*)) {
   override val nodeType: String = ViewLanguage.Hbox
}

object VBoxFactory extends AggregateFactory(new VBox(_: _*)) {
   override val nodeType: String = ViewLanguage.Vbox
}

object LabelFactory extends ViewFactory {
   override def fromXML(xmlnode: Node): Either[ViewParseError, scene.Node] = {
         val size: Int = xmlnode.attribute(ViewLanguage.FontSize).map(_.text.toInt).getOrElse(20) // TODO default font size
         val text: String = xmlnode.text
         val label = new Label(text)
         label.font = scalafx.scene.text.Font(label.font.name, size)
         Right(label)
      }

   override val nodeType: String = ViewLanguage.Label
}