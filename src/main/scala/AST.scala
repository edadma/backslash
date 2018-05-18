package xyz.hyperreal.backslash


trait AST

trait ExpressionAST extends AST

case class IfExpressionAST( cond: Seq[(ExpressionAST, ExpressionAST)], els: Option[ExpressionAST] ) extends ExpressionAST
case class BlockExpressionAST( statements: Vector[ExpressionAST] ) extends ExpressionAST
case class LiteralExpressionAST( v: Any ) extends ExpressionAST
case class VariableExpressionAST( v: String ) extends ExpressionAST
case class ExpressionExpressionAST( expr: ExpressionAST ) extends ExpressionAST
case class CommandExpressionAST( c: Command, args: List[ExpressionAST] ) extends ExpressionAST
