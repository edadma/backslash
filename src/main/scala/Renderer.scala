package xyz.hyperreal.backslash

import java.io.PrintStream

import scala.collection.mutable


class Renderer( config: Map[Symbol, Any], out: PrintStream ) {

  val vars = new mutable.HashMap[String, Any]

  def render( ast: AST ): Unit = {
    ast match {
      case BlockStatementAST( block ) => block foreach render
      case StaticStatementAST( s ) => out.print( s )
      case CommandStatementAST( c ) => c( config, vars, out, List(), null )
    }
  }

}