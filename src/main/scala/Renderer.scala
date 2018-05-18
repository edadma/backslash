//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( config: Map[Symbol, Any] ) {

  val vars = new mutable.HashMap[String, Any]

  def render( ast: AST, assigns: Map[String, Any], out: PrintStream ): Unit = {

    def render( ast: AST ): Unit =
      ast match {
        case BlockStatementAST( block ) => block foreach render
        case StaticStatementAST( s ) => out.print( s )
        case CommandStatementAST( c, args ) => c( config, vars, out, args map capture, null )
        case IfStatementAST( cond, els ) =>
          cond find { case (expr, _) => truthy( eval(expr) ) } match {
            case None =>
              els match {
                case None =>
                case Some( no ) => render( no )
              }
            case Some( (_, yes) ) => render( yes )
          }
      }

    def eval( expr: ExpressionAST ): Any =
      expr match {
        case VariableExpressionAST( v ) =>
          vars get v match {
            case None => nil
            case Some( a ) => a
          }
      }

    vars ++= assigns
    render( ast )
  }

  def capture( ast: AST ) = {
    val bytes = new ByteArrayOutputStream

    render( ast, Map(), new PrintStream(bytes) )
    bytes.toString
  }

}