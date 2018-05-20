package xyz.hyperreal.backslash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import util.parsing.input.{PagedSeq, PagedSeqReader, Position, Reader}


class Parser( commands: Map[String, Command] ) {

  type Input = Reader[Char]

  var csDelim = "\\"
  var beginDelim = "{"
  var endDelim = "}"

  case class Macro( parameters: Vector[String], body: AST )

  val macros = new mutable.HashMap[String, Macro]

  def parse( src: io.Source ): AST =
    parseBlock( new PagedSeqReader(PagedSeq.fromSource(src)) ) match {
      case (r1, b) if r1 atEnd => b
      case (r1, _) => problem( r1, s"expected end of input: $r1" )
    }

  def parseBlock( r: Input, v: Vector[AST] = Vector() ): (Input, BlockAST) =
    if (r atEnd)
      (r, BlockAST( v ))
    else if (r.first == '}')
      (r rest, BlockAST( v ))
    else
      parseStatement( r ) match {
        case (r1, null) => parseBlock( r1, v )
        case (r1, s) => parseBlock( r1, v :+ s )
      }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, AST) =
    if (r atEnd)
      (r, LiteralAST( buf toString ))
    else
      r first match {
        case '\\'|'}' => (r, LiteralAST( buf toString ))
        case c =>
          buf += c
          parseStatic( r.rest, buf )
      }

  def parseStatement( r: Input ): (Input, AST) =
    parseControlSequence( r ) match {
      case None => parseStatic( r )
      case Some( (r1, "delim") ) =>
        val (r2, c) = parseStringArgument( r1 )
        val (r3, b) = parseStringArgument( r2 )
        val (r4, e) = parseStringArgument( r3 )

        csDelim = c
        beginDelim = b
        endDelim = e
        (r4, null)
      case Some( (r1, "def") ) =>
        val (r2, v) = parseStrings( r1 )

        if (v isEmpty)
          problem( r1.pos, "expected name of macro" )

        val name = v.head

        if (r2.atEnd || r2.first != '{')
          problem( r2.pos, s"expected body of definition for $name" )

        val (r3, body) = parseRenderedArgument( r2 )

        macros(name) = Macro( v.tail, body )
        (r3, null)
      case Some( (r1, name) ) => parseCommand( r.pos, name, r1 )
    }

  def parseStrings( r: Input, v: Vector[String] = Vector() ): (Input, Vector[String]) = {
    val r1 = skipSpace( r )

    if (r1.atEnd || r1.first == '{')
      (r1, v)
    else
      parseString( r1 ) match {
        case (r2, s) => parseStrings( r2, v :+ s )
      }
  }

  def nameFirst( ch: Char ) = ch.isLetter || ch == '_'

  def nameRest( ch: Char ) = ch.isLetterOrDigit || ch == '_'

  def parseControlSequence( r: Input ): Option[(Input, String)] =
    if (!r.atEnd && r.first == '\\') {
      val (r1, s) =
        if (nameFirst( r.rest.first ))
          consume( r.rest, nameRest(_) )
        else if (r.rest.first.isWhitespace)
          (r.rest, " ")
        else if (r.rest.first.isDigit)
          problem( r.rest.pos, s"control sequence name can't start with a digit" )
        else
          consume( r.rest, c => !nameRest(c) && !c.isWhitespace )

      Some( (skipSpace(r1), s) )
    } else
      None

  def matches( r: Input, s: String, idx: Int = 0 ): Option[Input] =
    if (idx == s.length)
      Some( r )
    else if (r.atEnd || r.first != s.charAt( idx ))
      None
    else
      matches( r.rest, s, idx + 1 )

  def consume( r: Input, set: Char => Boolean, buf: StringBuilder = new StringBuilder ): (Input, String) =
    if (r atEnd)
      (r, buf toString)
    else
      r first match {
        case c if set( c ) =>
          buf += c
          consume( r.rest, set, buf )
        case _ => (r, buf toString)
      }

  def consumeDelimited( r: Input, delim: Char ): (Input, String) = {
    val (r1, s) = consume( r, _ != delim )

    (r1.rest, s)
  }

  def parseLiteral( r: Input ): (Input, Any) =
    r.first match {
      case '"' => consumeDelimited( r.rest, '"' )
      case '\'' => consumeDelimited( r.rest, '\'' )
      case '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9' =>
        consume( r, c => c.isDigit || c == '.' ) match {
          case (r1, n) => (r1, BigDecimal( n ))
        }
      case _ => parseString( r )
    }

  def parseString( r: Input ) = consume( r, !_.isWhitespace )

  def parseStringArgument( r: Input ): (Input, String) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected string argument" )

    parseString( r1 )
  }

  def parseRenderedArgument( r: Input ): (Input, AST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    parseControlSequence( r1 ) match {
      case None =>
        r1 first match {
          case '{' => parseBlock( r1.rest )
          case _ =>
            val (r2, s) = parseLiteral( r1 )

            (r2, LiteralAST( s ))
        }
      case Some( (r2, name) ) => parseCommand( r1.pos, name, r2 )
    }
  }

  def parseExpressionArgument( r: Input ): (Input, AST) = {
    if (r atEnd)
      problem( r, "expected command argument" )

    parseControlSequence( r ) match {
      case None =>
        r first match {
          case '{' => parseBlock( r.rest )
          case '"'|'\''|'0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9' =>
            val (r1, s) = parseLiteral( r )

            (r1, LiteralAST( s ))
          case _ =>
            val (r1, s) = parseString( r )

            (r1, VariableAST( s ))
        }
      case Some( (r1, name) ) => parseCommand( r.pos, name, r1 )
    }
  }

  def parseArguments( r: Input, n: Int, buf: ListBuffer[AST] = new ListBuffer[AST] ): (Input, List[AST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseRenderedArgument( r )

      buf += s
      parseArguments( r1, n - 1, buf )
    }
  }

  def skipSpace( r: Input ): Input =
    if (r.atEnd || !r.first.isWhitespace)
      r
    else
      skipSpace( r.rest )

  def parseCommand( pos: Position, name: String, r: Input ): (Input, AST) = {
    name match {
      case " " => (r, LiteralAST( " " ))
      case "if" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, body) = parseRenderedArgument( r1 )
        val (r3, elsifs) = parseCases( "elsif", r2 )
        val conds = (expr, body) +: elsifs

        parseElse( r3 ) match {
          case Some( (r4, els) ) => (r4, IfAST( conds, Some(els) ))
          case _ => (r3, IfAST( conds, None ))
        }
      case "unless" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, body) = parseRenderedArgument( r1 )

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
        val (r2, body) = parseRenderedArgument( r1 )

        parseElse( r2 ) match {
          case Some( (r3, els) ) => (r3, ForAST( r0.pos, expr, body, Some(els) ))
          case _ => (r2, ForAST( r0.pos, expr, body, None ))
        }
      case _ =>
        macros get name match {
          case None =>
            commands get name match {
              case None => (r, VariableAST( name ))
              case Some( c ) =>
                val (r1, args) = parseArguments( r, c.arity )

                (r1, CommandAST( pos, c, args ))
            }
          case Some( Macro(parameters, body) ) =>
            if (parameters isEmpty)
              (r, body)
            else {
              val (r1, args) = parseArguments( r, parameters.length )

              (r1, MacroAST( body, parameters zip args toMap ))
            }
        }
    }
  }

  def parseCases( cs: String, r: Input, elsifs: Vector[(AST, AST)] = Vector() ): (Input, Vector[(AST, AST)]) =
    parseControlSequence( skipSpace(r) ) match {
      case Some( (r1, cs) ) =>
        val (r2, expr) = parseExpressionArgument( r1 )
        val (r3, yes) = parseRenderedArgument( r2 )

        parseCases( cs, r3, elsifs :+ (expr, yes) )
      case _ => (r, elsifs)
    }

  def parseElse( r: Input ): Option[(Input, AST)] =
    parseControlSequence( skipSpace(r) ) match {
      case Some( (r1, "else") ) => Some( parseRenderedArgument(r1) )
      case _ => None
    }

}