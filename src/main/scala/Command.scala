package xyz.hyperreal.backslash

import java.io.File
import java.time.{OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import scala.util.parsing.input.Position


abstract class Command( val name: String, var arity: Int ) extends ((Position, Renderer, List[Any], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

  val standard =
    List(

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
            case List( a, b ) => problem( pos, s"expected arguments <date> <format>, given $a $b" )
          }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          nil
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
          (number( args.head ), number( args.tail.head )) match {
            case (Some( a ), Some( b )) => a remainder b
            case _ => problem( pos, s"expected arguments <number> <number>: $args" )
          }
      },

      new Command( "+", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          (number( args.head ), number( args.tail.head )) match {
            case (Some( a ), Some( b )) => a + b
            case _ => problem( pos, s"expected arguments <number> <number>: $args" )
          }
      },

      new Command( "-", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          (number( args.head ), number( args.tail.head )) match {
            case (Some( a ), Some( b )) => a - b
            case _ => problem( pos, s"expected arguments <number> <number>: $args" )
          }
      },

      new Command( "*", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          (number( args.head ), number( args.tail.head )) match {
            case (Some( a ), Some( b )) => a * b
            case _ => problem( pos, s"expected arguments <number> <number>: $args" )
          }
      },

      new Command( "/", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          (number( args.head ), number( args.tail.head )) match {
            case (Some( a ), Some( b )) => a + b
            case _ => problem( pos, s"expected arguments <number> <number>: $args" )
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

      new Command( "in", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( v: String, s: Seq[_] ) =>
              if (renderer.scopes isEmpty) problem( pos, "not inside a loop" )

              renderer.ForGenerator( v, s )
            case List( a, b ) => problem( pos, s"expected arguments <variable name> <sequence>: $a, $b" )
          }
      },

      new Command( "set", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[Any], context: AnyRef ): Any =
          args match {
            case List( v: String, a: Any ) =>
              renderer.setVar( v, a )
              nil
            case List( a, b ) => problem( pos, s"expected arguments <variable name> <sequence>: $a, $b" )
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