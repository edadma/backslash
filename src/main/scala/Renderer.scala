//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( val parser: Parser, val config: Map[Symbol, Any] ) {

  val globals = new mutable.HashMap[String, Any]
  val scopes = new mutable.ArrayStack[mutable.HashMap[String, Any]]()

	def setVar( name: String, value: Any ): Unit =
		scopes find (_ contains name) match {
			case None => globals(name) = value
			case Some( scope ) => scope(name) = value
		}

	def getVar( name: String, locals: Map[String, Any] ) =
		scopes find (_ contains name) match {
			case None =>
        locals get name match {
          case None =>
            globals get name match {
              case None => nil
              case Some( v ) => v
            }
          case Some( v ) => v
        }
			case Some( scope ) => scope(name)
		}

	def enterScope( locals: String* ): Unit = scopes += mutable.HashMap( locals map (_ -> nil): _* )

	def exitScope: Unit = scopes pop

  def render( ast: AST, assigns: Map[String, Any], out: PrintStream ): Unit = {
    def output( ast: AST ) = out print eval( ast )

    globals ++= assigns

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
        val buf = new StringBuilder

				enterScope( "_i", "_idx" )

        eval( expr ) match {
          case s: Seq[Any] =>
		    		try {
              s.zipWithIndex foreach { case (e, idx) =>
						    try {
                  setVar( "_i", e )
                  setVar( "_idx", BigDecimal(idx) )
                  buf ++= seval( body )
                } catch {
                  case _: ContinueException =>
                }
              }
            } catch {
              case _: BreakException =>
            }
          case a => problem( pos, s"expected sequence: $a" )
        }

				exitScope
        buf.toString
			case BreakAST( pos ) =>
        if (scopes isEmpty)
          problem( pos, s"not inside a 'for' loop" )

        throw new BreakException
			case ContinueAST( pos ) =>
        if (scopes isEmpty)
          problem( pos, s"not inside a 'for' loop" )

        throw new ContinueException
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
        getVar( v, Map() )
    }

//  def capture( ast: AST ) = {
//    val bytes = new ByteArrayOutputStream
//
//    render( ast, Map(), new PrintStream(bytes) )
//    bytes.toString
//  }

	class BreakException extends RuntimeException

	class ContinueException extends RuntimeException

}