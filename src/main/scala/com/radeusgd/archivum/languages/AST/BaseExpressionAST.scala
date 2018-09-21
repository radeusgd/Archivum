package com.radeusgd.archivum.languages.AST

sealed abstract class Expression

case class Variable(name: String) extends Expression {
   override def toString: String = name
}

case class IntValue(value: Int) extends Expression {
   override def toString: String = value.toString
}

case class StringValue(value: String) extends Expression {
   override def toString: String = "\"" + value + "\""
}
case class FunctionCall(name: String, args: List[Expression]) extends Expression {
   override def toString: String = name + "(" + args.mkString(", ") + ")"
}