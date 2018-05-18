//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( config: Map[Symbol, Any] ) {

  val vars = new mutable.HashMap[String, Any]

  def render( ast: AST, assigns: Map[String, Any], out: PrintStream ): Unit = {

    def seval( ast: AST ) = eval( ast ).toString

    def teval( ast: AST ) = truthy( eval(ast) )

    def eval( ast: AST ): Any =
      ast match {
        case BlockAST( block ) => block map seval mkString
        case LiteralAST( v ) => v
        case CommandAST( pos, c, args ) => c( pos, config, vars, args map eval, null )
        case ForAST( pos, expr, body, None ) =>
          eval( expr ) match {
            case s: Seq[Any] =>
              s map { e =>
                vars("i") = e
                seval( body )
              } mkString
            case a => problem( pos, s"expected sequence: $a" )
          }
        case IfAST( cond, els ) =>
          cond find { case (expr, _) => teval( expr ) } match {
            case None =>
              els match {
                case None => nil
                case Some( no ) => eval( no )
              }
            case Some( (_, yes) ) => eval( yes )
          }
        case VariableAST( v ) =>
          vars get v match {
            case None => nil
            case Some( a ) => a
          }
      }

    vars ++= assigns
    out.print( eval(ast) )

  }

//  def capture( ast: AST ) = {
//    val bytes = new ByteArrayOutputStream
//
//    render( ast, Map(), new PrintStream(bytes) )
//    bytes.toString
//  }

}