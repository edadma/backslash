package xyz.hyperreal.backslash

import util.parsing.input.{PagedSeqReader, PagedSeq, Position, Reader}


class Parser(commands: Map[String, Command] ) {

  type Input = Reader[Char]

  def parse( src: io.Source ): AST =
    parseBlock( new PagedSeqReader(PagedSeq.fromSource(src)) ) match {
      case (r1, b) if r1 atEnd => b
      case (r1, _) => problem( r1, s"expected end of input: $r1" )
    }

  def parseBlock( r: Input, v: Vector[StatementAST] = Vector() ): (Input, BlockStatementAST) =
    if (r atEnd)
      (r, BlockStatementAST( v ))
    else if (r.first == '}')
      (r rest, BlockStatementAST( v ))
    else
      parseStatement( r ) match {
        case (r1, s) => parseBlock( r1, v :+ s )
      }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, StatementAST) =
    if (r atEnd)
      (r, StaticStatementAST( buf toString ))
    else
      r first match {
        case '\\' =>
          r.rest.first match {
            case c@('\\'|'{'|'}') =>
              buf += c
              parseStatic( r.rest.rest, buf )
            case _ => (r, StaticStatementAST( buf toString ))
          }
        case '}' => (r, StaticStatementAST( buf toString ))
        case c =>
          buf += c
          parseStatic( r.rest, buf )
      }

  def parseStatement( r: Input ): (Input, StatementAST) =
    r first match {
      case '\\' => parseCommand( r.rest )
      case _ => parseStatic( r )
    }

  def parseName( r: Input, buf: StringBuilder = new StringBuilder ): (Input, String) =
    if (r atEnd)
      (r, buf toString)
    else
      r first match {
        case c if c.isLetter =>
          buf += c
          parseName( r.rest, buf )
        case _ => (r, buf toString)
      }

  def parseCommand( r: Input ): (Input, StatementAST) = {
    val (r1, name) = parseName( r )

    commands get name match {
      case None => (r1, VariableStatementAST( name ))
      case Some( c ) => (r1, CommandStatementAST( c ))
    }
  }

}