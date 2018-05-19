package xyz.hyperreal.backslash

import scala.util.parsing.input.Position


trait AST

case class IfAST( cond: Seq[(AST, AST)], els: Option[AST] ) extends AST
case class MatchAST( expr: AST, cases: Seq[(AST, AST)], els: Option[AST] ) extends AST
case class UnlessAST( cond: AST, body: AST, els: Option[AST] ) extends AST
case class ForAST( pos: Position, expr: AST, body: AST, els: Option[AST] ) extends AST
case class BlockAST( statements: Seq[AST] ) extends AST
case class LiteralAST( v: Any ) extends AST
case class VariableAST( v: String ) extends AST
case class CommandAST( pos: Position, c: Command, args: List[AST] ) extends AST
