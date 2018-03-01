package com.radeusgd.archivum.gui.layout

// TODO maybe make it checked?
case class LayoutParseError(message: String, cause: Option[Throwable] = None)
   extends RuntimeException(message, cause.orNull)
