package xyz.hyperreal.backslash

import java.io.File
import java.time.{OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import scala.util.matching.Regex
import scala.util.parsing.input.Position


abstract class Command( val name: String, var arity: Int ) extends ((Position, Renderer, List[AST], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

  class Const[T] {
    private var set = false
    private var value: T = _

    def apply( v: => T ) = {
      if (!set)
        synchronized {
          if (!set) {
            value = v
            set = true
          }
        }

      value
    }
  }

  val standard =
    List(

      new Command( "n", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          "\n"
      },

      new Command( "t", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          "\t"
      },

      new Command( "true", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          true
      },

      new Command( "false", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          false
      },

      new Command( "null", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          null
      },

      new Command( "today", 0 ) {
        val format = new Const[DateTimeFormatter]

        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          ZonedDateTime.now.format( format(DateTimeFormatter.ofPattern(renderer.config("today").toString)) )
      },

      new Command( "now", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          OffsetDateTime.now
      },

      new Command( "date", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( format: String, date: TemporalAccessor ) => DateTimeFormatter.ofPattern( format ).format( date )
            case List( a, b ) => problem( pos, s"expected arguments <format> <date>, given $a, $b" )
          }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any = {
          renderer.eval( args.head )
          nil
        }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: Seq[_] ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <separator> <sequence>, given $a, $b" )
          }
      },

      new Command( "map", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String, t: Traversable[_] ) =>
              t.asInstanceOf[Traversable[Map[String, Any]]] map (_(s))
//            case List( v: VariableAST, t: Traversable[_] ) =>
//              t.asInstanceOf[Traversable[Map[String, Any]]] map (_(v.name))
            case List( a, b ) => problem( pos, s"expected arguments <variable> <sequence>, given $a, $b" )
          }
      },

      new Command( "take", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) => s take n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <number> <sequence>, given $a, $b" )
          }
      },

      new Command( "drop", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) => s drop n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <number> <sequence>, given $a, $b" )
          }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: Seq[_], sep: String ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <separator>, given $a, $b" )
          }
      },

      new Command( "split", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: String ) => s split sep toList
            case List( sep: String, s: String ) => sep split s toList
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <regex> <string>, given $a, $b" )
          }
      },

      new Command( "regex", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s.r
            case List( a ) => problem( pos, s"expected string argument, given $a" )
          }
      },

      new Command( "u", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal ) if n.isValidChar => n.toChar
            case List( n: BigDecimal ) => problem( pos, s"number not a valid character: $n" )
            case List( a ) => problem( pos, s"expected number argument, given $a" )
          }
      },

      new Command( "number", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) =>
              number( s ) match {
                case None => problem( pos, s"not a number: $s" )
                case Some( n ) => n
              }
            case List( a ) => problem( pos, s"expected string argument, given $a" )
          }
      },

      new Command( "string", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          display( renderer.eval(args.head) )
      },

      new Command( "reverse", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s reverse
            case List( s: Seq[_] ) => s reverse
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "size", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s length
            case List( s: Seq[_] ) => s length
            case List( s: collection.Map[_, _] ) => s size
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "include", 1 ) {
        val dir = new Const[String]

        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( renderer.parser.parse(io.Source.fromFile(new File(dir(renderer.config("include").toString), renderer.eval(args.head).toString))) )
      },

      new Command( "rem", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a remainder b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "contains", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a contains b
            case List( a: Seq[_], b: String ) => a contains b
            case List( a: Map[_, _], b: String ) => a.asInstanceOf[Map[String, Any]] contains b
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <sequence> <string>: $a, $b" )
          }
      },

      new Command( "+", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a + b
            case List( a: String, b: String ) => a + b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "-", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a - b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "*", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a * b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "/", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a / b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args.head ) == renderer.eval( args.tail.head )
      },

      new Command( "/=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args.head ) != renderer.eval( args.tail.head )
      },

      new Command( "<", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a < b
            case List( a: BigDecimal, b: BigDecimal ) => a < b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a > b
            case List( a: BigDecimal, b: BigDecimal ) => a > b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "<=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a <= b
            case List( a: BigDecimal, b: BigDecimal ) => a <= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a >= b
            case List( a: BigDecimal, b: BigDecimal ) => a >= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "..", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], context: AnyRef ): Any =
          renderer.eval( args ) match {
            case List( start: BigDecimal, end: BigDecimal ) => start to end by 1
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      }

    ) map (c => c.name -> c) toMap

}