package com.radeusgd.archivum.languages

import org.parboiled2.{ParserInput, Rule1}

class QueryGrammar(input: ParserInput) extends BaseExpressionGrammar(input) {
   def Statement: Rule1[AST.Query] = rule {
      zeroOrMore(WhitespaceChar) ~ Query ~ EOI
   }

   def Query: Rule1[AST.Query] = rule {
      ws('A') ~> AST.BaseQuery // TODO
   }
}
