package com.radeusgd.archivum.datamodel

sealed abstract class ValidationError {
   def extendPath(s: String): ValidationError
}

case class TypeError(path: List[String], got: String, expected: String)
   extends ValidationError {
   override def extendPath(s: String): ValidationError =
      TypeError(s :: path, got, expected)
   override def toString: String =
      "Got " + got + ", but expected " + expected
}

case class ConstraintError(path: List[String], message: String)
   extends ValidationError {
   override def extendPath(s: String): ValidationError =
      ConstraintError(s :: path, message)
}