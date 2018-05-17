//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( config: Map[Symbol, Any] ) {

  val vars = new mutable.HashMap[String, Any]

  def render( ast: AST, out: PrintStream ): Unit = {

    def render( ast: AST ): Unit =
      ast match {
        case BlockStatementAST( block ) => block foreach render
        case StaticStatementAST( s ) => out.print( s )
        case CommandStatementAST( c, args ) => c( config, vars, out, args map capture, null )
      }

    render( ast )
  }

  def capture( ast: AST ) = {
    val bytes = new ByteArrayOutputStream

    render( ast, new PrintStream(bytes) )
    bytes.toString
  }

}