package xyz.hyperreal.backslash


trait AST

trait StatementAST extends AST

case class BlockStatementAST( statements: Vector[StatementAST] ) extends StatementAST
case class StaticStatementAST( s: String ) extends StatementAST
case class VariableStatementAST( v: String ) extends StatementAST
case class ExpressionStatementAST( expr: ExpressionAST ) extends StatementAST
case class CommandStatementAST( c: Command, args: List[String] ) extends StatementAST

trait ExpressionAST extends AST

case class VariableExpressionAST( v: String ) extends ExpressionAST