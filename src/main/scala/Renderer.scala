//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}

import scala.collection.mutable


class Renderer( val parser: Parser, val config: Map[String, Any] ) {

  val globals = new mutable.HashMap[String, Any]
  val scopes = new mutable.ArrayStack[mutable.HashMap[String, Any]]

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

  def render( ast: AST, assigns: collection.Map[String, Any], out: PrintStream ): Unit = {
    def output( ast: AST ) = {
      out print deval( ast )
      out.flush
    }

    globals ++= assigns

    ast match {
      case GroupAST( b ) => b foreach output
      case _ => output( ast )
    }
  }

  def capture( ast: AST, assigns: collection.Map[String, Any] )( implicit codec: io.Codec ) = {
		val bytes = new ByteArrayOutputStream

    render( ast, assigns, new PrintStream(bytes, false, codec.name) )
    bytes.toString
  }

  def deval( ast: AST ) = display( eval(ast) )

  def teval( ast: AST ) = truthy( eval(ast) )

  def seval( ast: AST ) = eval( ast ).asInstanceOf[Traversable[Any]]

  def eval( l: List[AST] ): List[Any] = l map eval

  def eval( ast: AST ): Any =
    ast match {
      case SetAST( v, expr ) =>
        setVar( v, eval(expr) )
        nil
      case InAST( cpos, v, epos, expr ) =>
        eval( expr ) match {
          case s: Seq[_] =>
            if (scopes isEmpty) problem( cpos, "not inside a loop" )

            ForGenerator( v, s )
          case a => problem( epos, s"expected a sequence: $a" )
        }
      case DotAST( epos, expr, kpos, key ) =>
        eval( expr ) match {
          case m: collection.Map[_, _] =>
            m.asInstanceOf[collection.Map[Any, Any]] get eval(key) match {
              case None => problem( kpos, "field not found" )
              case Some( v ) => v
            }
          case o => problem( epos, s"not a map: $o" )
        }
      case NotAST( expr ) => !teval( expr )
      case AndAST( left, right ) => teval( left ) && teval( right )
      case OrAST( left, right ) =>
        val l = eval( left )

        if (truthy( l ))
          l
        else {
          val r = eval( right )

          if (truthy( r ))
            r
          else
            false
        }
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
      case GroupAST( statements ) =>
        if (statements.length == 1)
          eval( statements.head )
        else
          statements map deval mkString
      case LiteralAST( v ) => v
      case CommandAST( pos, c, args, optional ) => c( pos, this, args, optional map {case (k, v) => (k -> eval(v))}, null )
      case ForAST( pos, expr, body, els ) =>
        val buf = new StringBuilder

				enterScope

        val (in, seq) =
          eval( expr ) match {
            case ForGenerator( v, s ) => (Some( v ), s)
            case s: Seq[Any] => (None, s)
            case m: collection.Map[_, _] => (None, List( m ))
            case a => problem( pos, s"expected sequence or map: $a" )
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

              buf ++= deval( body )
            } catch {
              case _: ContinueException =>
            }
          }

          els match {
            case None =>
            case Some( after ) => buf ++= deval( after )
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

  private class BreakException extends RuntimeException

  private class ContinueException extends RuntimeException

  private case class ForGenerator( v: String, s: Seq[Any] )

}