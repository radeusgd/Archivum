package com.radeusgd.archivum.languages.AST

sealed class Expression
case class Variable(name: String) extends Expression
case class IntValue(value: Int) extends Expression
case class StringValue(value: String) extends Expression
case class FunctionCall(name: String, args: List[Expression]) extends Expression {
   override def toString: String = name + "(" + args.mkString(",") + ")"
}