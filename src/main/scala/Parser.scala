package xyz.hyperreal.backslash

import scala.collection.mutable.ListBuffer
import util.parsing.input.{PagedSeq, PagedSeqReader, Position, Reader}


class Parser( commands: Map[String, Command] ) {

  type Input = Reader[Char]

  def parse( src: io.Source ): AST =
    parseBlock( new PagedSeqReader(PagedSeq.fromSource(src)) ) match {
      case (r1, b) if r1 atEnd => b
      case (r1, _) => problem( r1, s"expected end of input: $r1" )
    }

  def parseBlock( r: Input, v: Vector[ExpressionAST] = Vector() ): (Input, BlockExpressionAST) =
    if (r atEnd)
      (r, BlockExpressionAST( v ))
    else if (r.first == '}')
      (r rest, BlockExpressionAST( v ))
    else
      parseStatement( r ) match {
        case (r1, s) => parseBlock( r1, v :+ s )
      }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, ExpressionAST) =
    if (r atEnd)
      (r, LiteralExpressionAST( buf toString ))
    else
      r first match {
        case '\\' =>
          r.rest.first match {
            case c@('\\'|'{'|'}') =>
              buf += c
              parseStatic( r.rest.rest, buf )
            case _ => (r, LiteralExpressionAST( buf toString ))
          }
        case '}' => (r, LiteralExpressionAST( buf toString ))
        case c =>
          buf += c
          parseStatic( r.rest, buf )
      }

  def parseStatement( r: Input ): (Input, ExpressionAST) =
    parseOptionalControlSequence( r ) match {
      case None => parseStatic( r )
      case Some( (r1, name) ) => parseCommand( name, r1 )
    }

  def parseOptionalControlSequence( r: Input ): Option[(Input, String)] =
    if (!r.atEnd && r.first == '\\' && r.rest.first.isLetterOrDigit)
      Some( parseName(r.rest) )
    else
      None

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

  def parseName( r: Input ): (Input, String) = consume( r, _.isLetterOrDigit )

  def parseString( r: Input ): (Input, String) =
    r.first match {
      case '"' => consumeDelimited( r.rest, '"' )
      case '\'' => consumeDelimited( r.rest, '\'' )
      case _ => consume( r, !_.isWhitespace )
    }

  def parseRenderedArgument( r: Input ): (Input, ExpressionAST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    r1 first match {
      case '{' => parseBlock( r1.rest )
      case _ =>
        val (r2, s) = parseString( r1 )

        (r2, LiteralExpressionAST( s ))
    }
  }

  def parseStaticArgument( r: Input ): (Input, String) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    r1 first match {
      case '{' => consumeDelimited( r1.rest, '}' )
      case _ => parseString( r1 )
    }
  }

  def parseArguments( r: Input, n: Int, buf: ListBuffer[ExpressionAST] = new ListBuffer[ExpressionAST] ): (Input, List[ExpressionAST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseRenderedArgument( r )

      buf += s
      parseArguments( r1, n - 1, buf )
    }
  }

  def skipSpace( r: Input ): Input = consume( r, _.isWhitespace )._1

  def parseCommand( name: String, r: Input ): (Input, ExpressionAST) = {
    name match {
      case "if" =>
        val (r1, s) = parseStaticArgument( r )
        val (r2, yes) = parseRenderedArgument( r1 )

        parseOptionalControlSequence( skipSpace(r2) ) match {
          case Some( (r3, "else") ) =>
            val (r4, no) = parseRenderedArgument( r3 )

            (r4, IfExpressionAST( List((VariableExpressionAST(s), yes)), Some(no) ))
          case _ => (r2, IfExpressionAST( List((VariableExpressionAST(s), yes)), None ))
        }

      case _ =>
        commands get name match {
          case None => (r, VariableExpressionAST( name ))
          case Some( c ) =>

            val (r1, args) = parseArguments( r, c.arity )

            (r1, CommandExpressionAST( c, args ))
        }
    }
  }

}