//@
package xyz.hyperreal.backslash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import util.parsing.input.{PagedSeq, PagedSeqReader, Position, Reader}


class Parser( commands: Map[String, Command] ) {

  type Input = Reader[Char]

  var csDelim = "\\"
  var beginDelim = "{"
  var endDelim = "}"
  var pipeDelim = "|"

  val varRegex = """\.([^.]*)"""r
  val unicodeRegex = "\\\\u[0-9a-fA-F]{4}".r

  def escapes( s: String ) =
    unicodeRegex.replaceAllIn(
      s
        .replace( """\b""", "\b" )
        .replace( """\t""", "\t" )
        .replace( """\f""", "\f" )
        .replace( """\n""", "\n" )
        .replace( """\r""", "\r" )
        .replace( """\\""", "\\" )
        .replace( """\"""", "\"" )
        .replace( """\'""", "\'" ),
      m => Integer.parseInt( m.matched.substring(2), 16 ).toChar.toString
    )

  case class Macro( parameters: Vector[String], body: AST )

  val macros = new mutable.HashMap[String, Macro]

  def parse( src: io.Source ): AST =
    parseStatements( new PagedSeqReader(PagedSeq.fromSource(src)) ) match {
      case (r1, b) if r1 atEnd => b
      case (r1, _) => problem( r1, s"expected end of input: $r1" )
    }

  /*
        parseStatement( r ) match {
        case (r1, null) => parseStatements( r1, v )
        case (r1, s) =>
          matches( r1, pipeDelim ) match {
            case None => parseStatements( r1, v :+ s )
            case Some( r2 ) => stage( r2 )
          }
      }

   */
  def parseStatements( r: Input, v: Vector[AST] = Vector() ): (Input, GroupAST) =
    if (r.atEnd || lookahead( r, endDelim ))
      (r, GroupAST( v ))
    else
      parseStatement( r ) match {
        case (r1, null) => parseStatements( r1, v )
        case (r1, s) => parseStatements( r1, v :+ s )
      }

  def parseGroup( r: Input, v: Vector[AST] = Vector() ): (Input, GroupAST) = {
    val (r1, g) = parseStatements( r )

    matches( r1, endDelim ) match {
      case Some( r2 ) => (r2, g)
      case None =>
        problem( r, "unexpected end of input" )
    }
  }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, AST) =
    if (r atEnd)
      (r, LiteralAST( buf toString ))
    else if (lookahead( r, csDelim ) || lookahead( r, endDelim ) || lookahead( r, beginDelim ))
      (r, LiteralAST( buf toString ))
    else {
      buf += r.first
      parseStatic( r.rest, buf )
    }

  def parseStatement( r: Input ): (Input, AST) =
    parseControlSequence( r ) match {
      case None =>
        matches( r, beginDelim ) match {
          case None => parseStatic( r )
          case Some( r1 ) => parseGroup( r1 )
        }
      case Some( (r1, "#") ) =>
        matches( skip( r1, lookahead(_, csDelim + "#") ), csDelim + "#" ) match {
          case None => problem( r, "unclosed comment" )
          case Some( r2 ) => (r2, null)
        }
      case Some( (r1, "delim") ) =>
        val (r2, c) = parseStringArgument( r1 )
        val (r3, b) = parseStringArgument( r2 )
        val (r4, e) = parseStringArgument( r3 )

        csDelim = c
        beginDelim = b
        endDelim = e
        (r4, null)
      case Some( (r1, "def") ) =>
        val (r2, v) = parseStringArguments( r1 )

        if (v isEmpty)
          problem( r1.pos, "expected name of macro" )

        val name = v.head

        if (r2.atEnd || lookahead( r2, beginDelim ))
          problem( r2.pos, s"expected body of definition for $name" )

        val (r3, body) = parseRegularArgument( r2 )

        macros(name) = Macro( v.tail, body )
        (r3, null)
      case Some( (r1, name) ) => parseCommand( r.pos, name, r1, true )
    }

  def parseStringArguments( r: Input, v: Vector[String] = Vector() ): (Input, Vector[String]) = {
    val r1 = skipSpace( r )

    if (r1.atEnd || lookahead( r1, beginDelim ))
      (r1, v)
    else
      parseString( r1 ) match {
        case (r2, s) => parseStringArguments( r2, v :+ s )
      }
  }

  def nameFirst( c : Char ) = c.isLetter || c == '_'

  def nameRest( c: Char ) = c.isLetterOrDigit || c == '_' || c == '.'

  def parseControlSequence( r: Input ): Option[(Input, String)] =
    if (r.atEnd)
      None
    else
      matches( r, csDelim ) match {
        case Some( r1 ) =>
          val (r2, s) =
            if (nameFirst( r1.first ))
              consume( r1, nameRest )
            else if (r1.first.isWhitespace)
              (r1.rest, " ")
            else if (r1.first.isDigit)
              problem( r1.pos, s"control sequence name can't start with a digit" )
            else
              consume( r1, c => !(c.isLetterOrDigit || c == '_' || c.isWhitespace) )

          Some( (skipSpace(r2), s) )
        case None => None
      }

  def matches( r: Input, s: String, idx: Int = 0 ): Option[Input] =
    if (idx == s.length)
      Some( r )
    else if (r.atEnd || r.first != s.charAt( idx ))
      None
    else
      matches( r.rest, s, idx + 1 )

  def lookahead( r: Input, s: String ) = matches( r, s ) nonEmpty

  def consumeCond( r: Input, cond: Input => Boolean, buf: StringBuilder = new StringBuilder ): (Input, String) =
    if (cond( r )) {
      buf += r.first
      consumeCond( r.rest, cond, buf )
    } else
      (r, buf toString)

  def consume( r: Input, set: Char => Boolean, buf: StringBuilder = new StringBuilder ): (Input, String) = consumeCond( r, r => !r.atEnd && set(r.first), buf )

  def consumeStringLiteral( r: Input ) = {
    val del = r.first
    var first = true
    var prev = ' '

    def cond( cr: Input ) = {
      val res =
        if (cr atEnd)
          problem( r, "unclosed string literal" )
        else
          cr first match {
            case '\\' if cr.rest.atEnd => problem( cr, "unclosed string literal" )
            case `del` if !first && prev == '\\' => true
            case `del` => false
            case _ => true
          }

      first = false
      prev = cr.first
      res
    }

    val (r1, s) = consumeCond( r.rest, cond )

    (r1.rest, escapes( s ))
  }

  def parseLiteralArgument( r: Input ): (Input, Any) =
    r.first match {
      case '"'|'\'' => consumeStringLiteral( r )
      case '0' if !r.rest.atEnd && r.rest.first == 'x' =>
        consume( r.rest.rest, "0123456789abcdefABCDEF" contains _) match {
          case (r1, s) => (r1, BigDecimal( Integer.parseInt(s, 16)) )
        }
      case '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9' =>
        consume( r, c => c.isDigit || c == '.' ) match {
          case (r1, n) if isNumber( n ) => (r1, BigDecimal( n ))
          case s => s
        }
      case _ => parseString( r )
    }

  def parseString( r: Input ) = consumeCond( r, r => !r.atEnd && !r.first.isWhitespace && !lookahead(r, endDelim) )

  def parseStringWhitespace( r: Input ) = consume( r, !_.isWhitespace )

  def parseStringArgument( r: Input ): (Input, String) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected string argument" )

    parseStringWhitespace( r1 )
  }

  def parseRegularArgument( r: Input ): (Input, AST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    parseControlSequence( r1 ) match {
      case None =>
        matches( r1, beginDelim ) match {
          case Some( r2 ) => parseGroup( r2 )
          case None =>
            val (r2, s) = parseLiteralArgument( r1 )

            (r2, LiteralAST( s ))
        }
      case Some( (r2, name) ) => parseCommand( r1.pos, name, r2, false )
    }
  }

  def dotExpression( pos: Position, v: String ) = {
    def fields( start: Int, expr: AST ): AST =
      v.indexOf( '.', start ) match {
        case -1 => expr
        case dot if dot == start + 1 || dot == v.length - 1 => problem( pos, "illegal variable reference" )
        case dot =>
          v.indexOf( '.', dot + 1 ) match {
            case -1 => DotAST( pos, expr, pos, v.substring(dot + 1) )
            case idx => fields( idx + 1, DotAST(pos, expr, pos, v.substring(dot + 1, idx)) )
          }
      }

    fields( 0, VariableAST(
      v indexOf '.' match {
        case -1 => v
        case dot => v.substring( 0, dot )
      }) )
  }

  def parseVariableArgument( r: Input ) = {
    val res@(_, s) = parseString( r )

    if (!nameFirst( s.head ) || !s.tail.forall( nameRest ))
      problem( r, "illegal variable name" )

    check( r.pos, s )
    res
  }

  def parseExpressionArgument( r: Input ): (Input, AST) = {
    val r0 = skipSpace( r )

    if (r0 atEnd)
      problem( r0, "expected command argument" )

    parseControlSequence( r0 ) match {
      case None =>
        matches( r0, beginDelim ) match {
          case Some( r1 ) => parseGroup( r1 )
          case None =>
            if (nameFirst( r0 first )) {
              val (r1, s) = parseVariableArgument( r0 )

              (r1, dotExpression( r0.pos, s ))
            } else {
              val (r1, s) = parseLiteralArgument( r0 )

              (r1, LiteralAST( s ))
            }
        }
      case Some( (r1, name) ) => parseCommand( r0.pos, name, r1, false )
    }
  }

  def parseRegularArguments( r: Input, n: Int, buf: ListBuffer[AST] = new ListBuffer[AST] ): (Input, List[AST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseRegularArgument( r )

      buf += s
      parseRegularArguments( r1, n - 1, buf )
    }
  }

  def parseExpressionArguments( r: Input, n: Int, buf: ListBuffer[AST] = new ListBuffer[AST] ): (Input, List[AST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseExpressionArgument( r )

      buf += s
      parseExpressionArguments( r1, n - 1, buf )
    }
  }

  def skipSpace( r: Input ): Input = skip( r, !_.first.isWhitespace )

  def skip( r: Input, cond: Input => Boolean ): Input = if (r.atEnd || cond( r )) r else skip( r.rest, cond )

  def check( pos: Position, name: String ) =
    if (Set( "if", "for", "unless", "match", "set", "in", "and", "or", "not" ) contains name)
      problem( pos, "illegal variable name, it's a reserved word" )
    else if (commands contains name)
      problem( pos, "illegal variable name, it's a command" )

  def parseCommand( pos: Position, name: String, r: Input, statement: Boolean ): (Input, AST) = {
    name match {
      case "<<<" =>
        var first = true
        var prev = ' '

        def cond( cr: Input ) = {
          val res =
            if (cr atEnd)
              problem( r, "unclosed raw text" )
            else
              !lookahead( cr, csDelim + ">>>" )

          first = false
          prev = cr.first
          res
        }

        val (r1, s) = consumeCond( r, cond )

        (r1, LiteralAST( s ))
      case "." =>
        val (r1, ast) = parseExpressionArgument( r )
        val r2 = skipSpace( r1 )
        val (r3, a) = parseRegularArgument( r1 )

        (r3, DotAST( r.pos, ast, r2.pos, a ))
      case "set" =>
        val (r1, v) = parseVariableArgument( r )
        val (r2, ast) = parseExpressionArgument( r1 )

        (r2, SetAST( v, ast ))
      case "in" =>
        val (r1, v) = parseVariableArgument( r )
        val r2 = skipSpace( r1 )
        val (r3, ast) = parseExpressionArgument( r2 )

        (r3, InAST( pos, v, r2.pos, ast ))
      case "not" =>
        val (r1, expr) = parseExpressionArgument( r )

        (r1, NotAST( expr ))
      case "and" =>
        val (r1, args) = parseExpressionArguments( r, 2 )

        (r1, AndAST( args.head, args.tail.head ))
      case "or" =>
        val (r1, args) = parseExpressionArguments( r, 2 )

        (r1, OrAST( args.head, args.tail.head ))
      case " " => (r, LiteralAST( " " ))
      case "if" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, body) = parseRegularArgument( r1 )
        val (r3, elsifs) = parseCases( "elsif", r2 )
        val conds = (expr, body) +: elsifs

        parseElse( r3 ) match {
          case Some( (r4, els) ) => (r4, IfAST( conds, Some(els) ))
          case _ => (r3, IfAST( conds, None ))
        }
      case "unless" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, body) = parseRegularArgument( r1 )

        parseElse( r2 ) match {
          case Some( (r3, els) ) => (r3, UnlessAST( expr, body, Some(els) ))
          case _ => (r2, UnlessAST( expr, body, None ))
        }
      case "match" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, cases) = parseCases( "case", r1 )

        parseElse( r2 ) match {
          case Some( (r3, els) ) => (r3, MatchAST( expr, cases, Some(els) ))
          case _ => (r2, MatchAST( expr, cases, None ))
        }
      case "for" =>
        val r0 = skipSpace( r )
        val (r1, expr) = parseExpressionArgument( r0 )
        val (r2, body) = parseRegularArgument( r1 )

        parseElse( r2 ) match {
          case Some( (r3, els) ) => (r3, ForAST( r0.pos, expr, body, Some(els) ))
          case _ => (r2, ForAST( r0.pos, expr, body, None ))
        }
      case "break" => (r, BreakAST( pos ))
      case "continue" => (r, ContinueAST( pos ))
      case _ =>
        val res@(r2, ast) =
          macros get name match {
            case None =>
              commands get name match {
                case None => (r, dotExpression( pos, name ))
                case Some( c ) =>
                  val (r1, args) = parseRegularArguments( r, c.arity )

                  (r1, CommandAST( pos, c, args ))
              }
            case Some( Macro(parameters, body) ) =>
              if (parameters isEmpty)
                (r, body)
              else {
                val (r1, args) = parseRegularArguments( r, parameters.length )

                (r1, MacroAST( body, parameters zip args ))
              }
          }

        def filters( r: Input, ast: AST ): (Input, AST) =
          matches( skipSpace(r), pipeDelim ) match {
            case None => (r, ast)
            case Some( r1 ) =>
              val r2 = skipSpace( r1 )

              parseControlSequence( r2 ) match {
                case None => problem( r2, "expected a control sequence" )
                case Some( (r3, name) ) =>
                  macros get name match {
                    case None =>
                      commands get name match {
                        case None => problem( r2, "expected a command or macro" )
                        case Some( c ) if c.arity == 0 => problem( r2, "expected a command with parameters" )
                        case Some( c ) =>
                          val (r4, args) = parseRegularArguments( r3, c.arity - 1 )

                          filters(r4, CommandAST( r2.pos, c, args :+ ast ))
                      }
                    case Some( Macro(parameters, _) ) if parameters isEmpty => problem( r2, "expected a macro with parameters" )
                    case Some( Macro(parameters, body) ) =>
                      val (r4, args) = parseRegularArguments( r3, parameters.length - 1 )

                      filters(r4, MacroAST( body, parameters zip (args :+ ast) ))
                  }
              }
          }

        if (statement)
          filters( r2, ast )
        else
          res
    }
  }

  def parseCases( cs: String, r: Input, cases: Vector[(AST, AST)] = Vector() ): (Input, Vector[(AST, AST)]) =
    parseControlSequence( skipSpace(r) ) match {
      case Some( (r1, `cs`) ) =>
        val (r2, expr) = parseExpressionArgument( r1 )
        val (r3, yes) = parseRegularArgument( r2 )

        parseCases( cs, r3, cases :+ (expr, yes) )
      case _ => (r, cases)
    }

  def parseElse( r: Input ): Option[(Input, AST)] =
    parseControlSequence( skipSpace(r) ) match {
      case Some( (r1, "else") ) => Some( parseRegularArgument(r1) )
      case _ => None
    }

}