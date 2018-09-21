package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.languages.{AST, QueryLanguage}
import com.radeusgd.archivum.languages.AST.{FunctionCall, IntValue, StringValue, Variable}

class Expression(root: AST.Expression) {
   def evaluate(ctx: DMValue): DMValue =
      root match {
         case Variable(name) => DMUtils.makeGetter(name)(ctx)
         case IntValue(value) => DMInteger(value)
         case StringValue(value) => DMString(value)
         case FunctionCall(name, args) =>
            val evaluatedArgs: List[DMValue] = args.map(arg => new Expression(arg).evaluate(ctx))
            Expression.functions(name)(evaluatedArgs)
      }
}

object Expression {
   val functions: Map[String, (List[DMValue]) => DMValue] = Map(
      "+" -> {
         case List(DMString(a), DMString(b))=> DMString(a + b)
         case List(DMInteger(a), DMInteger(b)) => DMInteger(a + b)
      },
      "-" -> {
         case List(DMInteger(a), DMInteger(b)) => DMInteger(a - b)
      },
      "*" -> {
         case List(DMInteger(a), DMInteger(b)) => DMInteger(a * b)
      },
      "/" -> {
         case List(DMInteger(a), DMInteger(b)) => DMInteger(a / b)
      },
      QueryLanguage.ArrayLength -> {
         case List(array: DMArray) => DMInteger(array.length)
      },
   )
}