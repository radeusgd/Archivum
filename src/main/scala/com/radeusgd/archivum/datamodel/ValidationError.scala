package com.radeusgd.archivum.datamodel

sealed abstract class ValidationError {
   def extendPath(s: String): ValidationError
   def getPath: List[String]
   def getMessage: String
}

object ValidationError {
   def describe(errors: List[ValidationError]): String =
      errors.map(e => e.getPath.mkString(".") + " - " + e.toString).mkString("\n")
}

case class TypeError(path: List[String], got: String, expected: String)
   extends ValidationError {
   override def extendPath(s: String): ValidationError =
      TypeError(s :: path, got, expected)

   override def getPath: List[String] = path

   override def getMessage: String = toString

   override def toString: String =
      "Got " + got + ", but expected " + expected
}

case class ConstraintError(path: List[String], message: String)
   extends ValidationError {
   override def extendPath(s: String): ValidationError =
      ConstraintError(s :: path, message)

   override def getPath: List[String] = path

   override def getMessage: String = message
}