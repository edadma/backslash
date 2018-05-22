package xyz.hyperreal.backslash

import java.io.File
import java.time.{OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import scala.util.matching.Regex
import scala.util.parsing.input.Position


abstract class Command( val name: String, var arity: Int ) extends ((Position, Renderer, List[Any], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

  val standard =
    List(

      new Command( "n", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          "\n"
      },

      new Command( "t", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          "\t"
      },

      new Command( "true", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          true
      },

      new Command( "false", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          false
      },

      new Command( "null", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          null
      },

      new Command( "today", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( renderer.config('today).asInstanceOf[DateTimeFormatter] )
      },

      new Command( "now", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          OffsetDateTime.now
      },

      new Command( "date", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( date: TemporalAccessor, format: String ) => DateTimeFormatter.ofPattern( format ).format( date )
            case List( a, b ) => problem( pos, s"expected arguments <date> <format>, given $a, $b" )
          }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          nil
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: Seq[_], sep: String ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <separator>, given $a, $b" )
          }
      },

      new Command( "take", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: Seq[_], n: BigDecimal ) => s take n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <number>, given $a, $b" )
          }
      },

      new Command( "drop", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: Seq[_], n: BigDecimal ) => s drop n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <number>, given $a, $b" )
          }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: Seq[_], sep: String ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <separator>, given $a, $b" )
          }
      },

      new Command( "split", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: String, sep: String ) => s split sep toList
            case List( s: String, sep: Regex ) => sep split s toList
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <string> <regex>, given $a, $b" )
          }
      },

      new Command( "regex", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( s: String ) => s.r
            case List( a ) => problem( pos, s"expected string argument, given $a" )
          }
      },

      new Command( "not", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          falsy( args.head )
      },

      new Command( "reverse", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args.head match {
            case List( s: String ) => s reverse
            case List( s: Seq[_] ) => s reverse
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "size", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args.head match {
            case List( s: String ) => s length
            case List( s: Seq[_] ) => s length
            case List( s: collection.Map[_, _] ) => s size
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "include", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          renderer.eval( renderer.parser.parse(io.Source.fromFile(new File(renderer.config('include).toString, args.head.toString))) )
      },

      new Command( "rem", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: BigDecimal, b: BigDecimal ) => a remainder b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "contains", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: String, b: String ) => a contains b
            case List( a: Seq[_], b: String ) => a contains b
            case List( a: Map[_, _], b: String ) => a.asInstanceOf[Map[String, Any]] contains b
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <sequence> <string>: $a, $b" )
          }
      },

      new Command( "+", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: BigDecimal, b: BigDecimal ) => a + b
            case List( a: String, b: String ) => a + b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "-", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: BigDecimal, b: BigDecimal ) => a - b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "*", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: BigDecimal, b: BigDecimal ) => a * b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "/", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: BigDecimal, b: BigDecimal ) => a / b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args.head == args.tail.head
      },

      new Command( "/=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args.head != args.tail.head
      },

      new Command( "<", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: String, b: String ) => a < b
            case List( a: BigDecimal, b: BigDecimal ) => a < b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: String, b: String ) => a > b
            case List( a: BigDecimal, b: BigDecimal ) => a > b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "<=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: String, b: String ) => a <= b
            case List( a: BigDecimal, b: BigDecimal ) => a <= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( a: String, b: String ) => a >= b
            case List( a: BigDecimal, b: BigDecimal ) => a >= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "..", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( start: BigDecimal, end: BigDecimal ) => start to end by 1
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      }

    ) map (c => c.name -> c) toMap

}