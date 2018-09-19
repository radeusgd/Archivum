package com.radeusgd.archivum.languages

import org.parboiled2._

import scala.util.{Failure, Success, Try}


class BaseExpressionGrammar(val input: ParserInput) extends Parser {

   /*implicit def wspStr(s: String): Rule0 = rule {
      str(s) ~ zeroOrMore(WhitespaceChar)
   }*/

   def Expression: Rule1[AST.Expression] = rule {
      (Term ~ ws('+') ~ Term ~> ((a: AST.Expression, b: AST.Expression) => AST.FunctionCall("+", List(a, b)))) |
      (Term ~ ws('-') ~ Term ~> ((a: AST.Expression, b: AST.Expression) => AST.FunctionCall("-", List(a, b)))) |
      Term
   }

   def Term: Rule1[AST.Expression] = rule {
      (Factor ~ ws('*') ~ Factor ~> ((a: AST.Expression, b: AST.Expression) => AST.FunctionCall("*", List(a, b)))) |
      (Factor ~ ws('/') ~ Factor ~> ((a: AST.Expression, b: AST.Expression) => AST.FunctionCall("/", List(a, b)))) |
      Factor
   }

   def Factor: Rule1[AST.Expression] = rule { Number | Parens /*| FunctionCall */}

   /*def FunctionCall: Rule1[AST.Expression] = rule {
      capture(Identifier) ~ '(' ~ (Expression * ',') ~ ')'
         ~> ((name: String) => AST.IntValue(0))
   }*/

   def Parens: Rule1[AST.Expression] = rule { ws('(') ~ Expression ~ ws(')') }

   def Number: Rule1[AST.Expression] = rule {
      capture(oneOrMore(CharPredicate.Digit)) ~ zeroOrMore(WhitespaceChar) ~> ((d: String) => AST.IntValue(d.toInt))
   }

   def Text: Rule1[AST.Expression] = rule {
      ws('"') ~ capture(zeroOrMore(CharPredicate.All)) ~ ws('"') ~> ((quot: String) => AST.StringValue(quot))
   }

   def Identifier: Rule1[String] = rule {
      capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum)) ~ zeroOrMore(WhitespaceChar)
   }

   def ws(c: Char): Rule0 = rule { c ~ zeroOrMore(WhitespaceChar) }

   val WhitespaceChar = CharPredicate(" \\n\\r\\t\\f")
}

class TestExtendedGrammar(input: ParserInput) extends BaseExpressionGrammar(input) {
   def Statement: Rule1[AST.Expression] = rule {
      zeroOrMore(WhitespaceChar) ~ Expression ~ EOI
   }
}

object Test {

   def simpleParseTest(input: String): Unit = {
      println(s"Parsing: '$input'")
      val parser = new TestExtendedGrammar(input)
      val result: Try[AST.Expression] = parser.Statement.run()
      result match {
         case Failure(error) => error match {
            case pe: ParseError => println(parser.formatError(pe))
            case _ => println(error)
         }
         case Success(i) => println(i)
      }
   }

   def main(args: Array[String]): Unit = {
      simpleParseTest("1+2")
      simpleParseTest("1 + 2")
      simpleParseTest("1+2*(3+4)")
   }
}