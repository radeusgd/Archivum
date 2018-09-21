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

   def Factor: Rule1[AST.Expression] = rule { FunctionCall | Text | Variable | Number | Parens }

   def FunctionCall: Rule1[AST.Expression] = rule {
      Identifier ~ FunctionCallArgs ~> ((name: String, args: Seq[AST.Expression]) => AST.FunctionCall(name, args.toList))
   }

   def FunctionCallArgs: Rule1[Seq[AST.Expression]] = rule {
      ws('(') ~ (Expression * ws(',').named(",")) ~ ws(')')
   }

   def Parens: Rule1[AST.Expression] = rule { ws('(') ~ Expression ~ ws(')') }

   def Number: Rule1[AST.Expression] = rule {
      capture(oneOrMore(CharPredicate.Digit)) ~ Whitespace ~> ((d: String) => AST.IntValue(d.toInt))
   }

   def Text: Rule1[AST.Expression] = rule {
      '"' ~ capture(zeroOrMore(CharPredicate.from(_ != '"'))) ~ ws('"') ~> AST.StringValue
   }

   def Variable: Rule1[AST.Expression] = rule {
      (Identifier | '`' ~ capture(oneOrMore(CharPredicate.from(_ != '`'))) ~ ws('`')) ~> AST.Variable
   }

   def Identifier: Rule1[String] = rule {
      capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum)) ~ Whitespace
   }

   def Whitespace: Rule0 = rule {
      zeroOrMore(quiet(WhitespaceChar))
   }

   def ws(c: Char): Rule0 = rule { c ~ Whitespace }

   val WhitespaceChar = CharPredicate(" \n\r\t\f")
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
            case pe: ParseError => println(parser.formatError(pe, new ErrorFormatter(showTraces = false)))
            case _ => println(error)
         }
         case Success(i) => println(i)
      }
   }

   def main(args: Array[String]): Unit = {
      simpleParseTest("""("abc" + "def")""")
      simpleParseTest("1 + 2")
      simpleParseTest("1+2*(3+4)")
      simpleParseTest("f(0)")
      simpleParseTest("f(x)")
      simpleParseTest("f(1,2 + 3) * test(\"abc\", a, 2)")
   }
}