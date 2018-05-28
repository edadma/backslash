package xyz.hyperreal.backslash

import java.io.File
import java.time.{Instant, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import scala.util.parsing.input.Position
import collection.immutable.IntMap
import collection.mutable
import xyz.hyperreal.json.DefaultJSONReader


abstract class Command( val name: String, var arity: Int ) extends ((Position, Renderer, List[AST], Map[String, Any], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

//  val entities = {
//    val map = new mutable.HashMap[Int, String]
//    val json = DefaultJSONReader.fromSource( io.Source.fromInputStream(classOf[Command].getResourceAsStream("entities.json")) )
//
//    for ((k, v) <- json)
//      if (map contains k)
//
//    println( json )
//  }

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

  def invoke( renderer: Renderer, lambda: AST, arg: Any ) = {
    renderer.enterScope
    renderer.scopes.top("_") = arg

    val res = renderer.eval( lambda )

    renderer.exitScope
    res
  }

  val standard =
    List(

      new Command( "n", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          "\n"
      },

      new Command( "t", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          "\t"
      },

      new Command( "true", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          true
      },

      new Command( "false", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          false
      },

      new Command( "null", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          null
      },

      new Command( "today", 0 ) {
        val format = new Const[DateTimeFormatter]

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( format(DateTimeFormatter.ofPattern(renderer.config("today").toString)) )
      },

      new Command( "now", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          ZonedDateTime.now
      },

      new Command( "date", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( format: String, date: TemporalAccessor ) => DateTimeFormatter.ofPattern( format ).format( date )
            case List( a, b ) => problem( pos, s"expected arguments <format> <date>, given $a, $b" )
          }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head )
          nil
        }
      },

      new Command( "abs", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case List( n: BigDecimal ) => n.abs
            case List( a ) => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "ceil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case List( n: BigDecimal ) =>
              if (n.isWhole)
                n
              else if (n > 0)
                n.toBigInt + 1
              else
                n.toBigInt
            case List( a ) => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "floor", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case List( n: BigDecimal ) =>
              if (n.isWhole)
                n
              else if (n > 0)
                n.toBigInt
              else
                n.toBigInt - 1
            case List( a ) => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "neg", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case List( n: BigDecimal ) => -n
            case List( a ) => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "min", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a min b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "max", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a max b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: Seq[_] ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <separator> <sequence>, given $a, $b" )
          }
      },

      new Command( "map", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val t = renderer.seval( args.tail.head )

          args.head match {
            case LiteralAST( s: String ) => t.asInstanceOf[Seq[Map[String, Any]]] map (_(s))
            case lambda => t map (invoke( renderer, lambda, _ ))
            case List(a, b) => problem(pos, s"expected arguments <variable> <sequence>, given $a, $b")
          }
        }
      },

      new Command( "take", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) => s take n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <number> <sequence>, given $a, $b" )
          }
      },

      new Command( "drop", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) => s drop n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <number> <sequence>, given $a, $b" )
          }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: Seq[_], sep: String ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <sequence> <separator>, given $a, $b" )
          }
      },

      new Command( "split", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: String ) => s split sep toList
            case List( sep: String, s: String ) => sep split s toList
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <regex> <string>, given $a, $b" )
          }
      },

      new Command( "regex", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s.r
            case List( a ) => problem( pos, s"expected string argument, given $a" )
          }
      },

      new Command( "u", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal ) if n.isValidChar => n.toChar
            case List( n: BigDecimal ) => problem( pos, s"number not a valid character: $n" )
            case List( a ) => problem( pos, s"expected number argument, given $a" )
          }
      },

      new Command( "number", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
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
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          display( renderer.eval(args.head) )
      },

      new Command( "reverse", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s reverse
            case List( s: Seq[_] ) => s reverse
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "sort", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val on = optional get "on" map (_.toString)
          val desc = (optional get "order" map (_.toString)) contains "desc"

          def comp( a: Any, b: Any ) =
            if (desc)
              !lt( a, b )
            else
              lt( a, b )

          def lt( a: Any, b: Any ) =
            (a, b) match {
              case (a: BigDecimal, b: BigDecimal) => a < b
              case (a: Instant, b: Instant) => a isBefore b
              case (a: ZonedDateTime, b: ZonedDateTime) => a isBefore b
              case (a: Instant, b: ZonedDateTime) => a isBefore b.toInstant
              case (a: ZonedDateTime, b: Instant) => a.toInstant isBefore b
              case _ => a.toString < b.toString
            }

          renderer.eval( args ) match {
            case List( s: Seq[_] ) if on isDefined => s.asInstanceOf[Seq[Map[String, Any]]] sortWith ((a, b) => comp(a(on.get), b(on.get)))
            case List( s: Seq[_] ) => s sortWith lt
            case List( a ) => problem( pos, s"expected sequence argument: $a" )
          }
        }
      },

      new Command( "head", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s head
            case List( s: Seq[_] ) => s head
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "last", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s last
            case List( s: Seq[_] ) => s last
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "tail", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s tail
            case List( s: Seq[_] ) => s tail
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "distinct", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: Seq[_] ) => s distinct
            case List( a ) => problem( pos, s"expected sequence argument: $a" )
          }
      },

      new Command( "lowercase", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s toLowerCase
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "trim", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s trim
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "uppercase", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s toUpperCase
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "size", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s length
            case List( s: Seq[_] ) => s length
            case List( s: collection.Map[_, _] ) => s size
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "include", 1 ) {
        val dir = new Const[String]

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( renderer.parser.parse(io.Source.fromFile(new File(dir(renderer.config("include").toString), renderer.eval(args.head).toString))) )
      },

      new Command( "rem", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a remainder b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "contains", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a contains b
            case List( a: Seq[_], b: String ) => a contains b
            case List( a: Map[_, _], b: String ) => a.asInstanceOf[Map[String, Any]] contains b
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <sequence> <string>: $a, $b" )
          }
      },

      new Command( "default", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer eval args match  {
            case List( a: Any, b: Any ) =>
              if (b == nil)
                a
              else
                b
          }
      },

      new Command( "append", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer eval args match  {
            case List( a: Any, b: Seq[_] ) => b :+ a
            case List( a: String, b: String ) => b + a
            case List( a, b ) => problem( pos, s"expected arguments <any> <sequence> or <string> <string>: $a, $b" )
          }
      },

      new Command( "+", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a + b
            case List( a: Seq[_], b: Seq[_] ) => a ++ b
            case List( a: Seq[_], b: Any ) => a :+ b
            case List( a: Any, b: Seq[_] ) => a +: b
            case List( a: String, b: String ) => a + b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "-", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a - b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "*", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a * b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "/", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a / b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "^", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) if b.isValidInt => a pow b.intValue
            case List( a, b ) => problem( pos, s"expected arguments <number> <integer>: $a, $b" )
          }
      },

      new Command( "=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args.head ) == renderer.eval( args.tail.head )
      },

      new Command( "/=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args.head ) != renderer.eval( args.tail.head )
      },

      new Command( "<", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a < b
            case List( a: BigDecimal, b: BigDecimal ) => a < b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a > b
            case List( a: BigDecimal, b: BigDecimal ) => a > b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "<=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a <= b
            case List( a: BigDecimal, b: BigDecimal ) => a <= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( ">=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a >= b
            case List( a: BigDecimal, b: BigDecimal ) => a >= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "..", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match {
            case List( start: BigDecimal, end: BigDecimal ) => start to end by 1
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      }

    ) map (c => c.name -> c) toMap

}