package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.EditableView

import scalafx.scene
import cats.implicits._

abstract class AggregateFactory(make: (Seq[scene.Node]) => scene.Node) extends LayoutFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] =
      if (xmlnode.attributes.nonEmpty) Left(LayoutParseError("Unrecognized attributes"))
      else {
         val childrenResults = xmlnode.child filter (_.label != "#PCDATA") map (EditableView.parseViewTree(_, ev))
         type ParsingEither[A] = Either[LayoutParseError, A]
         val children: Either[LayoutParseError, Seq[ParsedLayout]] =
            childrenResults.toList.sequence[ParsingEither, ParsedLayout]
         children.map(buildAggregate)
      }

   private def buildAggregate(views: Seq[ParsedLayout]): ParsedLayout = {
      val nodes = views map (_.node)
      val boundeds = views flatMap (_.boundControls)
      ParsedLayout(make(nodes), boundeds)
   }

   // TODO use scalaz or Cats ???
   private def eitherSequence[A, B](seq: Seq[Either[A, B]]): Either[A, Seq[B]] =
      seq.collectFirst({ case Left(l) => Left(l) }).getOrElse(
         Right(seq.collect({ case Right(r) => r }))
      )
}
