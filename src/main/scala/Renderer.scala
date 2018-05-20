//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( val parser: Parser, val config: Map[Symbol, Any] ) {

  val vars = new mutable.HashMap[String, Any]

  def render( ast: AST, assigns: Map[String, Any], out: PrintStream ): Unit = {
    def output( ast: AST ) = out print eval( ast )

    vars ++= assigns

    ast match {
      case BlockAST( b ) => b foreach output
      case _ => output( ast )
    }
  }

  def seval( ast: AST ) = eval( ast ).toString

  def teval( ast: AST ) = truthy( eval(ast) )

  def eval( ast: AST ): Any =
    ast match {
      case MacroAST( body, args ) =>

      case MatchAST( expr, cases, els ) =>
        val e = eval( expr )

        cases find { case (expr, _) => e == eval( expr ) } match {
          case None =>
            els match {
              case None => nil
              case Some( no ) => eval( no )
            }
          case Some( (_, yes) ) => eval( yes )
        }
      case BlockAST( block ) => block map seval mkString
      case LiteralAST( v ) => v
      case CommandAST( pos, c, args ) => c( pos, this, args map eval, null )
      case ForAST( pos, expr, body, None ) =>
        eval( expr ) match {
          case s: Seq[Any] =>
            s.zipWithIndex map { case (e, idx) =>
              vars("i") = e
              vars("idx") = BigDecimal( idx )
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
      case UnlessAST( cond, body, els ) =>
        if (!teval( cond ))
          eval( body )
        else
            els match {
              case None => nil
              case Some( yes ) => eval( yes )
            }
      case VariableAST( v ) =>
        vars get v match {
          case None => nil
          case Some( a ) => a
        }
    }

//  def capture( ast: AST ) = {
//    val bytes = new ByteArrayOutputStream
//
//    render( ast, Map(), new PrintStream(bytes) )
//    bytes.toString
//  }

}