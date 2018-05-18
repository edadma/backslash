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

  def parseBlock( r: Input, v: Vector[AST] = Vector() ): (Input, BlockAST) =
    if (r atEnd)
      (r, BlockAST( v ))
    else if (r.first == '}')
      (r rest, BlockAST( v ))
    else
      parseStatement( r ) match {
        case (r1, s) => parseBlock( r1, v :+ s )
      }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, AST) =
    if (r atEnd)
      (r, LiteralAST( buf toString ))
    else
      r first match {
        case '\\' =>
          r.rest.first match {
            case c@('\\'|'{'|'}') =>
              buf += c
              parseStatic( r.rest.rest, buf )
            case _ => (r, LiteralAST( buf toString ))
          }
        case '}' => (r, LiteralAST( buf toString ))
        case c =>
          buf += c
          parseStatic( r.rest, buf )
      }

  def parseStatement( r: Input ): (Input, AST) =
    parseOptionalControlSequence( r ) match {
      case None => parseStatic( r )
      case Some( (r1, name) ) => parseCommand( r.pos, name, r1 )
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

  def parseRenderedArgument( r: Input ): (Input, AST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    parseOptionalControlSequence( r1 ) match {
      case None =>
        r1 first match {
          case '{' => parseBlock( r1.rest )
          case _ =>
            val (r2, s) = parseString( r1 )

            (r2, LiteralAST( s ))
        }
      case Some( (r2, name) ) => parseCommand( r1.pos, name, r2 )
    }
  }

  def parseExpressionArgument( r: Input ): (Input, AST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    parseOptionalControlSequence( r1 ) match {
      case None =>
        r1 first match {
          case '{' => parseBlock( r1.rest )
          case '"'|'\'' =>
            val (r2, s) = parseString( r1 )

            (r2, LiteralAST( s ))
          case _ =>
            val (r2, s) = parseString( r1 )

            (r2, VariableAST( s ))
        }
      case Some( (r2, name) ) => parseCommand( r1.pos, name, r2 )
    }
  }

//  def parseStaticArgument( r: Input ): (Input, String) = {
//    val r1 = skipSpace( r )
//
//    if (r1 atEnd)
//      problem( r1, "expected command argument" )
//
//    r1 first match {
//      case '{' => consumeDelimited( r1.rest, '}' )
//      case _ => parseString( r1 )
//    }
//  }

  def parseArguments( r: Input, n: Int, buf: ListBuffer[AST] = new ListBuffer[AST] ): (Input, List[AST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseRenderedArgument( r )

      buf += s
      parseArguments( r1, n - 1, buf )
    }
  }

  def skipSpace( r: Input ): Input = consume( r, _.isWhitespace )._1

  def parseCommand( pos: Position, name: String, r: Input ): (Input, AST) = {
    name match {
      case "if" =>
        val (r1, expr) = parseExpressionArgument( r )
        val (r2, yes) = parseRenderedArgument( r1 )
        val (r3, elsifs) = parseElsif( r2 )
        val conds = (expr, yes) +: elsifs

        parseElse( r3 ) match {
          case Some( (r4, els) ) => (r4, IfAST( conds, Some(els) ))
          case _ => (r3, IfAST( conds, None ))
        }
      case _ =>
        commands get name match {
          case None => (r, VariableAST( name ))
          case Some( c ) =>

            val (r1, args) = parseArguments( r, c.arity )

            (r1, CommandAST( pos, c, args ))
        }
    }
  }

  def parseElsif( r: Input, elsifs: Vector[(AST, AST)] = Vector() ): (Input, Vector[(AST, AST)]) =
    parseOptionalControlSequence( skipSpace(r) ) match {
      case Some( (r1, "elsif") ) =>
        val (r2, expr) = parseExpressionArgument( r1 )
        val (r3, yes) = parseRenderedArgument( r2 )

        parseElsif( r3, elsifs :+ (expr, yes) )
      case _ => (r, elsifs)
    }

  def parseElse( r: Input ): Option[(Input, AST)] =
    parseOptionalControlSequence( skipSpace(r) ) match {
      case Some( (r1, "else") ) => Some( parseRenderedArgument(r1) )
      case _ => None
    }

}