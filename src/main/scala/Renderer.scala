//@
package xyz.hyperreal.backslash

import java.io.PrintStream

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

	def enterScope: Unit = scopes push new mutable.HashMap

	def exitScope: Unit = scopes pop

  def render( ast: AST, assigns: Map[String, Any], out: PrintStream ): Unit = {
    def output( ast: AST ) = out print eval( ast )

    globals ++= assigns

    ast match {
      case GroupAST( b ) => b foreach output
      case _ => output( ast )
    }
  }

  def seval( ast: AST ) = eval( ast ).toString

  def teval( ast: AST ) = truthy( eval(ast) )

  def eval( ast: AST ): Any =
    ast match {
      case AndAST( left, right ) => teval( left ) && teval( right )
      case OrAST( left, right ) => teval( left ) || teval( right )
      case MacroAST( body, args ) =>
        enterScope
        scopes.top ++= args map {case (k, v) => (k, eval(v))}

        val res = eval( body )

        exitScope
        res
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
      case GroupAST( block ) => block map seval mkString
      case LiteralAST( v ) => v
      case CommandAST( pos, c, args ) => c( pos, this, args map eval, null )
      case ForAST( pos, expr, body, None ) =>
        val buf = new StringBuilder

				enterScope

        val (in, seq) =
          eval( expr ) match {
            case ForGenerator( v, s ) => (Some( v ), s)
            case s: Seq[Any] => (None, s)
            case a => problem( pos, s"expected sequence: $a" )
          }

        try {
          seq.zipWithIndex foreach { case (e, idx) =>
            try {
              scopes.top(if (in isDefined) in.get else "_i") = e
              scopes.top("_idx") = BigDecimal( idx )

              if (in.isEmpty && e.isInstanceOf[collection.Map[_, _]])
                e.asInstanceOf[collection.Map[String, Any]] foreach {
                  case (k, v) => scopes.top(k) = v
                }

              buf ++= seval( body )
            } catch {
              case _: ContinueException =>
            }
          }
        } catch {
          case _: BreakException =>
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
      case VariableAST( v ) => getVar( v, Map() )
    }

	class BreakException extends RuntimeException

	class ContinueException extends RuntimeException

  case class ForGenerator( v: String, s: Seq[Any] )
}