//@
package xyz.hyperreal.backslash

import java.io.File
import java.time.{Instant, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.regex.Matcher

import scala.util.parsing.input.Position
import xyz.hyperreal.__markdown__._

import scala.util.matching.Regex


abstract class Command( val name: String, var arity: Int ) extends ((Position, Renderer, List[AST], Map[String, Any], AnyRef) => Any) {
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

  def invoke( renderer: Renderer, lambda: AST, arg: Any ) = {
    renderer.enterScope
    renderer.scopes.top("_") = arg

    val res = renderer.eval( lambda )

    renderer.exitScope
    res
  }

  val escapeRegex = """([^\w _.,!:;?-])"""r

  val standard =
    List(

      new Command( "*", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a * b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "+", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a + b
            case List( a: Map[_, _], b: Map[_, _] ) => a ++ b
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

      new Command( "..", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match {
            case List( start: BigDecimal, end: BigDecimal ) => start to end by 1
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

      new Command( "<=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a <= b
            case List( a: BigDecimal, b: BigDecimal ) => a <= b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number> or <string> <string>: $a, $b" )
          }
      },

      new Command( "=", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args.head ) == renderer.eval( args.tail.head )
      },

      new Command( ">", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a > b
            case List( a: BigDecimal, b: BigDecimal ) => a > b
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

      new Command( "[]", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          Nil
      },

      new Command( "^", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) if b.isValidInt => a pow b.intValue
            case List( a, b ) => problem( pos, s"expected arguments <number> <integer>: $a, $b" )
          }
      },

      new Command( "abs", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => n.abs
            case a => problem( pos, s"not a number: $a" )
          }
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

      new Command( "ceil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => n.setScale( 0, BigDecimal.RoundingMode.CEILING )
            case a => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "contains", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: String, b: String ) => a contains b
            case List( a: Seq[_], b ) => a contains b
            case List( a: Map[_, _], b ) => a.asInstanceOf[Map[Any, Any]] contains b
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <sequence> <any> or <object> <any>: $a, $b" )
          }
      },

      new Command( "date", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( format: String, date: TemporalAccessor ) => DateTimeFormatter.ofPattern( format ).format( date )
            case List( a, b ) => problem( pos, s"expected arguments <format> <date>, given $a, $b" )
          }
      },

      new Command( "default", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer eval args match  {
            case List( a: Any, b: Any ) => if (b == nil) a else b
          }
      },

      new Command( "distinct", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: Seq[_] ) => s distinct
            case List( a ) => problem( pos, s"expected sequence argument: $a" )
          }
      },

      new Command( "downcase", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s toLowerCase
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "drop", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) if n.isValidInt => s drop n.toInt
            case List( n: BigDecimal, s: String ) if n.isValidInt => s drop n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <integer> <sequence> or <integer> <string>, given $a, $b" )
          }
      },

      new Command( "escape", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case s: String => escape( s )
            case a => problem( pos, s"not a string: $a" )
          }
        }
      },

      new Command( "escapeOnce", 1 ) {
        val regex = """&#?\w+;"""r

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case s: String =>
              val it = regex.findAllIn( s )
              var last = 0
              val buf = new StringBuilder

              while (it hasNext) {
                val m = it.next

                buf ++= escape( s.substring(last, it.start) )
                buf ++= it.matched
                last = it.end
              }

              buf ++= escape( s.substring(last, s.length) )
              buf.toString
            case a => problem( pos, s"not a string: $a" )
          }
        }
      },

      new Command( "false", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          false
      },

      new Command( "filter", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          (args.head, renderer.eval( args.tail.head )) match {
            case (lambda, s: Seq[_]) => s filter (e => truthy( invoke(renderer, lambda, e)) )
            case (a, b) => problem( pos, s"expected arguments <lambda <sequence>, given $a, $b" )
          }
      },

      new Command( "floor", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => n.setScale( 0, BigDecimal.RoundingMode.FLOOR )
            case a => problem( pos, s"not a number: $a" )
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

      new Command( "include", 1 ) {
        val dir = new Const[String]

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val file = new File( dir(renderer.config("include").toString), renderer.eval(args.head).toString )
          val charset = optional get "charset" map (_.toString)

          renderer.eval( renderer.parser.parse(if (charset.isDefined) io.Source.fromFile(file) else io.Source.fromFile(file)(charset get)) )
        }
      },

      new Command( "integer", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val x = renderer.eval( args.head )

          number( x ) match {
            case None => problem( pos, s"not a number: $x" )
            case Some( n: BigDecimal ) if n.isWhole => n
            case Some( n: BigDecimal ) => n.setScale( 0, BigDecimal.RoundingMode.DOWN )
          }
        }
      },

      new Command( "join", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: Seq[_] ) => s mkString sep
            case List( a, b ) => problem( pos, s"expected arguments <separator> <sequence>, given $a, $b" )
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

      new Command( "map", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          (args.head, renderer.eval( args.tail.head )) match {
            case (LiteralAST( f: String ), s: Seq[_]) => s.asInstanceOf[Seq[Map[String, Any]]] map (_ getOrElse( f, nil ))
            case (lambda, s: Seq[_]) => s map (invoke( renderer, lambda, _ ))
            case (a, b) => problem( pos, s"expected arguments <variable> <sequence> or <lambda <sequence>>, given $a, $b" )
          }
      },

      new Command( "markdown", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          Markdown( renderer.eval(args.head).toString )
      },

      new Command( "max", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a max b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "min", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a min b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "n", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          "\n"
      },

      new Command( "negate", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => -n
            case a => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head )
          nil
        }
      },

      new Command( "normalize", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args.head ).toString.trim.replaceAll( """\s+""", " " )
      },

      new Command( "now", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          ZonedDateTime.now
      },

      new Command( "null", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          null
      },

      new Command( "number", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val x = renderer.eval( args.head )

          number( x ) match  {
            case None => problem( pos, s"not a number: $x" )
            case Some( n ) => n
          }
        }
      },

      new Command( "regex", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s.r
            case List( a ) => problem( pos, s"expected string argument, given $a" )
          }
      },

      new Command( "rem", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( a: BigDecimal, b: BigDecimal ) => a remainder b
            case List( a, b ) => problem( pos, s"expected arguments <number> <number>: $a, $b" )
          }
      },

      new Command( "remove", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r: String ) => r replace (l, "")
            case List( a, b ) => problem( pos, s"expected arguments <string> <string>: $a, $b" )
          }
      },

      new Command( "removeFirst", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r: String ) => r replaceFirst (Matcher.quoteReplacement(l), "")
            case List( a, b ) => problem( pos, s"expected arguments <string> <string>: $a, $b" )
          }
      },

      new Command( "replace", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l1: String, l2: String, r: String ) => r.replaceAll( l1, l2 )
            case List( l1: Regex, l2: String, r: String ) => l1.replaceAllIn( r, l2 )
            case List( a, b, c ) => problem( pos, s"expected arguments <string> <string> <string> or <regex> <string> <string>: $a, $b, $c" )
          }
      },

      new Command( "replaceFirst", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l1: String, l2: String, r: String ) => r replaceFirst (Matcher.quoteReplacement(l1), l2)
            case List( a, b, c ) => problem( pos, s"expected arguments <string> <string> <string>: $a, $b, $c" )
          }
      },

      new Command( "reverse", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s reverse
            case List( s: Seq[_] ) => s reverse
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "round", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          (renderer.eval( args.head ), optional get "scale" getOrElse 0) match {
            case (n: BigDecimal, scala: Number) => round( n, scala.intValue, renderer.config )
            case (a, b) => problem( pos, s"not a number: $a, $b" )
          }
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

      new Command( "slice", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args ) match  {
            case List( s: String, start: BigDecimal, end: BigDecimal ) if start.isValidInt && end.isValidInt => s.slice( start.intValue, end.intValue )
            case List( s: Seq[_], start: BigDecimal, end: BigDecimal ) if start.isValidInt && end.isValidInt => s.slice( start.intValue, end.intValue )
            case List( a, b ) => problem( pos, s"expected arguments <string> <start> <end> or <sequence> <start> <end>: $a, $b" )
          }
        }
      },

      new Command( "sort", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val on = optional get "on" map (_.toString)
          val desc = (optional get "order" map (_.toString)) contains "desc"

          def comp( a: Any, b: Any ) = if (desc) !lt( a, b ) else lt( a, b )

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

      new Command( "split", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( sep: String, s: String ) => s split sep toList
            case List( sep: Regex, s: String ) => sep split s toList
            case List( a, b ) => problem( pos, s"expected arguments <string> <string> or <regex> <string>, given $a, $b" )
          }
      },

      new Command( "string", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          display( renderer.eval(args.head) )
      },

      new Command( "t", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          "\t"
      },

      new Command( "tail", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s tail
            case List( s: Seq[_] ) => s tail
            case List( a ) => problem( pos, s"expected string or sequence argument: $a" )
          }
      },

      new Command( "take", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal, s: Seq[_] ) if n.isValidInt => s take n.toInt
            case List( n: BigDecimal, s: String ) if n.isValidInt => s take n.toInt
            case List( a, b ) => problem( pos, s"expected arguments <integer> <sequence> or <integer> <string>, given $a, $b" )
          }
      },

      new Command( "today", 0 ) {
        val format = new Const[DateTimeFormatter]

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( format(DateTimeFormatter.ofPattern(renderer.config("today").toString)) )
      },

      new Command( "trim", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s trim
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "true", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          true
      },

      new Command( "u", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( n: BigDecimal ) if n.isValidChar => n.toChar
            case List( n: BigDecimal ) => problem( pos, s"number not a valid character: $n" )
            case List( a ) => problem( pos, s"expected number argument, given $a" )
          }
      },

      new Command( "upcase", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( s: String ) => s toUpperCase
            case List( a ) => problem( pos, s"expected string argument: $a" )
          }
      },

      new Command( "{}", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          Map()
      },

    ) map (c => c.name -> c) toMap

  def escape( s: String ) =
    escapeRegex.replaceAllIn( s, { m => s"&${Entity( m group 1 head )};" } )

}