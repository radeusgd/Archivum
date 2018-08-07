package com.radeusgd.archivum.gui.layout

// TODO maybe make it checked?
case class LayoutParseError(message: String, cause: Option[Throwable] = None)
   extends RuntimeException(message, cause.orNull)

object LayoutParseError {
   def apply(message: String, cause: Option[Throwable] = None): LayoutParseError =
      new LayoutParseError(message, cause)
   def apply(node: xml.Node, message: String): LayoutParseError =
      LayoutParseError(node.toString() + "<" + node.label + "> " + message)
}