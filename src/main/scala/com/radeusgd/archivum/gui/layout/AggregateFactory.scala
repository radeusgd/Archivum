package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.EditableView

import scalafx.scene
import cats.implicits._
import com.radeusgd.archivum.gui.utils.XMLUtils

abstract class AggregateFactory(make: (Seq[scene.Node]) => scene.Node) extends LayoutFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout] =
      if (xmlnode.attributes.nonEmpty) Left(LayoutParseError("Unrecognized attributes"))
      else {
         val childrenResults = XMLUtils.properChildren(xmlnode).map(EditableView.parseViewTree(_, ev, prefix))
         type ParsingEither[A] = Either[LayoutParseError, A]
         val children: Either[LayoutParseError, Seq[ParsedLayout]] =
           eitherSequence(childrenResults.toList)
         children.map(buildAggregate)
      }

   private def buildAggregate(views: Seq[ParsedLayout]): ParsedLayout = {
      val nodes = views map (_.node)
      val boundeds = views flatMap (_.boundControls)
      ParsedLayout(make(nodes), boundeds)
   }

   private def eitherSequence[A, B](seq: Seq[Either[A, B]]): Either[A, Seq[B]] =
      seq.collectFirst({ case Left(l) => Left(l) }).getOrElse(
         Right(seq.collect({ case Right(r) => r }))
      )
}
