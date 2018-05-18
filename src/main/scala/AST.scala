package xyz.hyperreal.backslash

import scala.util.parsing.input.Position


trait AST

case class IfAST( cond: Seq[(AST, AST)], els: Option[AST] ) extends AST
case class ForAST( pos: Position, gen: AST, body: AST, els: Option[AST] ) extends AST
case class BlockAST( statements: Seq[AST] ) extends AST
case class LiteralAST( v: Any ) extends AST
case class VariableAST( v: String ) extends AST
case class ExpressionAST( expr: AST ) extends AST
case class CommandAST( pos: Position, c: Command, args: List[AST] ) extends AST
