//@
package xyz.hyperreal.backslash

import java.io.File
import java.time.{Instant, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.regex.Matcher

import scala.util.parsing.input.Position

import xyz.hyperreal.__markdown__._


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

  val nonWordCharacterRegex = """([^\w _.?])"""r

  val standard =
    List(

      new Command( "[]", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          Nil
      },

      new Command( "{}", 0 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          Map()
      },

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

      new Command( "normalize", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ).toString.trim.replaceAll( """\s+""", " " )
        }
      },

      new Command( "markdown", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          Markdown( renderer.eval(args.head).toString )
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

      new Command( "abs", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => n.abs
            case a => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "ceil", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal =>
              if (n.isWhole)
                n
              else if (n > 0)
                n.toBigInt + 1
              else
                n.toBigInt
            case a => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "floor", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal =>
              if (n.isWhole)
                n
              else if (n > 0)
                n.toBigInt
              else
                n.toBigInt - 1
            case a => problem( pos, s"not a number: $a" )
          }
        }
      },

      new Command( "neg", 1 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args.head ) match {
            case n: BigDecimal => -n
            case a => problem( pos, s"not a number: $a" )
          }
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

      new Command( "remove", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r: String ) => l replace (r, "")
            case List( a, b ) => problem( pos, s"expected arguments <string> <string>: $a, $b" )
          }
      },

      new Command( "removeFirst", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r: String ) => l replaceFirst (Matcher.quoteReplacement(r), "")
            case List( a, b ) => problem( pos, s"expected arguments <string> <string>: $a, $b" )
          }
      },

      new Command( "replace", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r1: String, r2: String ) => l replace (r1, r2)
            case List( a, b, c ) => problem( pos, s"expected arguments <string> <string> <string>: $a, $b, $c" )
          }
      },

      new Command( "replaceFirst", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          renderer.eval( args ) match  {
            case List( l: String, r1: String, r2: String ) => l replaceFirst (Matcher.quoteReplacement(r1), r2)
            case List( a, b, c ) => problem( pos, s"expected arguments <string> <string> <string>: $a, $b, $c" )
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
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          (args.head, renderer.eval( args.tail.head )) match {
            case (LiteralAST( f: String ), s: Seq[_]) => s.asInstanceOf[Seq[Map[String, Any]]] map (_ getOrElse( f, nil ))
            case (lambda, s: Seq[_]) => s map (invoke( renderer, lambda, _ ))
            case (a, b) => problem( pos, s"expected arguments <variable> <sequence> or <lambda <sequence>>, given $a, $b" )
          }
      },

      new Command( "filter", 2 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any =
          (args.head, renderer.eval( args.tail.head )) match {
            case (lambda, s: Seq[_]) => s filter (e => truthy( invoke(renderer, lambda, e)) )
            case (a, b) => problem( pos, s"expected arguments <lambda <sequence>, given $a, $b" )
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
            case List( a ) =>
              println( a.getClass)
              problem( pos, s"expected string or sequence argument: $a" )
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

        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          val file = new File( dir(renderer.config("include").toString), renderer.eval(args.head).toString )
          val charset = optional get "charset" map (_.toString)

          renderer.eval( renderer.parser.parse(if (charset.isDefined) io.Source.fromFile(file) else io.Source.fromFile(file)(charset get)) )
        }
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

      new Command( "slice", 3 ) {
        def apply( pos: Position, renderer: Renderer, args: List[AST], optional: Map[String, Any], context: AnyRef ): Any = {
          renderer.eval( args ) match  {
            case List( s: String, start: BigDecimal, end: BigDecimal ) if start.isWhole && end.isWhole => s.slice( start.intValue, end.intValue )
            case List( s: Seq[_], start: BigDecimal, end: BigDecimal ) if start.isWhole && end.isWhole => s.slice( start.intValue, end.intValue )
            case List( a, b ) => problem( pos, s"expected arguments <string> <start> <end> or <sequence> <start> <end>: $a, $b" )
          }
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

  def escape( s: String ) =
    nonWordCharacterRegex.replaceSomeIn( s,
      { m =>
        val c = m group 1 head

        val entity =
          c match {
            case '\u0009' => "Tab"
            case '\u000A' => "NewLine"
            case '\u0021' => "excl"
            case '\u0022' => "quot"
            case '\u0023' => "num"
            case '\u0024' => "dollar"
            case '\u0025' => "percnt"
            case '\u0026' => "amp"
            case '\'' => "apos"
            case '\u0028' => "lpar"
            case '\u0029' => "rpar"
            case '\u002A' => "ast"
            case '\u002B' => "plus"
            case '\u002C' => "comma"
            case '\u002E' => "period"
            case '\u002F' => "sol"
            case '\u003A' => "colon"
            case '\u003B' => "semi"
            case '\u003C' => "lt"
            case '\u003D' => "equals"
            case '\u003E' => "gt"
            case '\u003F' => "quest"
            case '\u0040' => "commat"
            case '\u005B' => "lsqb"
            case '\\' => "bsol"
            case '\u005D' => "rsqb"
            case '\u005E' => "Hat"
            case '\u005F' => "lowbar"
            case '\u0060' => "grave"
            case '\u007B' => "lcub"
            case '\u007C' => "verbar"
            case '\u007D' => "rcub"
            case '\u00A0' => "nbsp"
            case '\u00A1' => "iexcl"
            case '\u00A2' => "cent"
            case '\u00A3' => "pound"
            case '\u00A4' => "curren"
            case '\u00A5' => "yen"
            case '\u00A6' => "brvbar"
            case '\u00A7' => "sect"
            case '\u00A8' => "Dot"
            case '\u00A9' => "copy"
            case '\u00AA' => "ordf"
            case '\u00AB' => "laquo"
            case '\u00AC' => "not"
            case '\u00AD' => "shy"
            case '\u00AE' => "reg"
            case '\u00AF' => "macr"
            case '\u00B0' => "deg"
            case '\u00B1' => "plusmn"
            case '\u00B2' => "sup2"
            case '\u00B3' => "sup3"
            case '\u00B4' => "acute"
            case '\u00B5' => "micro"
            case '\u00B6' => "para"
            case '\u00B7' => "middot"
            case '\u00B8' => "cedil"
            case '\u00B9' => "sup1"
            case '\u00BA' => "ordm"
            case '\u00BB' => "raquo"
            case '\u00BC' => "frac14"
            case '\u00BD' => "frac12"
            case '\u00BE' => "frac34"
            case '\u00BF' => "iquest"
            case '\u00C0' => "Agrave"
            case '\u00C1' => "Aacute"
            case '\u00C2' => "Acirc"
            case '\u00C3' => "Atilde"
            case '\u00C4' => "Auml"
            case '\u00C5' => "Aring"
            case '\u00C6' => "AElig"
            case '\u00C7' => "Ccedil"
            case '\u00C8' => "Egrave"
            case '\u00C9' => "Eacute"
            case '\u00CA' => "Ecirc"
            case '\u00CB' => "Euml"
            case '\u00CC' => "Igrave"
            case '\u00CD' => "Iacute"
            case '\u00CE' => "Icirc"
            case '\u00CF' => "Iuml"
            case '\u00D0' => "ETH"
            case '\u00D1' => "Ntilde"
            case '\u00D2' => "Ograve"
            case '\u00D3' => "Oacute"
            case '\u00D4' => "Ocirc"
            case '\u00D5' => "Otilde"
            case '\u00D6' => "Ouml"
            case '\u00D7' => "times"
            case '\u00D8' => "Oslash"
            case '\u00D9' => "Ugrave"
            case '\u00DA' => "Uacute"
            case '\u00DB' => "Ucirc"
            case '\u00DC' => "Uuml"
            case '\u00DD' => "Yacute"
            case '\u00DE' => "THORN"
            case '\u00DF' => "szlig"
            case '\u00E0' => "agrave"
            case '\u00E1' => "aacute"
            case '\u00E2' => "acirc"
            case '\u00E3' => "atilde"
            case '\u00E4' => "auml"
            case '\u00E5' => "aring"
            case '\u00E6' => "aelig"
            case '\u00E7' => "ccedil"
            case '\u00E8' => "egrave"
            case '\u00E9' => "eacute"
            case '\u00EA' => "ecirc"
            case '\u00EB' => "euml"
            case '\u00EC' => "igrave"
            case '\u00ED' => "iacute"
            case '\u00EE' => "icirc"
            case '\u00EF' => "iuml"
            case '\u00F0' => "eth"
            case '\u00F1' => "ntilde"
            case '\u00F2' => "ograve"
            case '\u00F3' => "oacute"
            case '\u00F4' => "ocirc"
            case '\u00F5' => "otilde"
            case '\u00F6' => "ouml"
            case '\u00F7' => "divide"
            case '\u00F8' => "oslash"
            case '\u00F9' => "ugrave"
            case '\u00FA' => "uacute"
            case '\u00FB' => "ucirc"
            case '\u00FC' => "uuml"
            case '\u00FD' => "yacute"
            case '\u00FE' => "thorn"
            case '\u00FF' => "yuml"
            case '\u0100' => "Amacr"
            case '\u0101' => "amacr"
            case '\u0102' => "Abreve"
            case '\u0103' => "abreve"
            case '\u0104' => "Aogon"
            case '\u0105' => "aogon"
            case '\u0106' => "Cacute"
            case '\u0107' => "cacute"
            case '\u0108' => "Ccirc"
            case '\u0109' => "ccirc"
            case '\u010A' => "Cdot"
            case '\u010B' => "cdot"
            case '\u010C' => "Ccaron"
            case '\u010D' => "ccaron"
            case '\u010E' => "Dcaron"
            case '\u010F' => "dcaron"
            case '\u0110' => "Dstrok"
            case '\u0111' => "dstrok"
            case '\u0112' => "Emacr"
            case '\u0113' => "emacr"
            case '\u0116' => "Edot"
            case '\u0117' => "edot"
            case '\u0118' => "Eogon"
            case '\u0119' => "eogon"
            case '\u011A' => "Ecaron"
            case '\u011B' => "ecaron"
            case '\u011C' => "Gcirc"
            case '\u011D' => "gcirc"
            case '\u011E' => "Gbreve"
            case '\u011F' => "gbreve"
            case '\u0120' => "Gdot"
            case '\u0121' => "gdot"
            case '\u0122' => "Gcedil"
            case '\u0124' => "Hcirc"
            case '\u0125' => "hcirc"
            case '\u0126' => "Hstrok"
            case '\u0127' => "hstrok"
            case '\u0128' => "Itilde"
            case '\u0129' => "itilde"
            case '\u012A' => "Imacr"
            case '\u012B' => "imacr"
            case '\u012E' => "Iogon"
            case '\u012F' => "iogon"
            case '\u0130' => "Idot"
            case '\u0131' => "imath"
            case '\u0132' => "IJlig"
            case '\u0133' => "ijlig"
            case '\u0134' => "Jcirc"
            case '\u0135' => "jcirc"
            case '\u0136' => "Kcedil"
            case '\u0137' => "kcedil"
            case '\u0138' => "kgreen"
            case '\u0139' => "Lacute"
            case '\u013A' => "lacute"
            case '\u013B' => "Lcedil"
            case '\u013C' => "lcedil"
            case '\u013D' => "Lcaron"
            case '\u013E' => "lcaron"
            case '\u013F' => "Lmidot"
            case '\u0140' => "lmidot"
            case '\u0141' => "Lstrok"
            case '\u0142' => "lstrok"
            case '\u0143' => "Nacute"
            case '\u0144' => "nacute"
            case '\u0145' => "Ncedil"
            case '\u0146' => "ncedil"
            case '\u0147' => "Ncaron"
            case '\u0148' => "ncaron"
            case '\u0149' => "napos"
            case '\u014A' => "ENG"
            case '\u014B' => "eng"
            case '\u014C' => "Omacr"
            case '\u014D' => "omacr"
            case '\u0150' => "Odblac"
            case '\u0151' => "odblac"
            case '\u0152' => "OElig"
            case '\u0153' => "oelig"
            case '\u0154' => "Racute"
            case '\u0155' => "racute"
            case '\u0156' => "Rcedil"
            case '\u0157' => "rcedil"
            case '\u0158' => "Rcaron"
            case '\u0159' => "rcaron"
            case '\u015A' => "Sacute"
            case '\u015B' => "sacute"
            case '\u015C' => "Scirc"
            case '\u015D' => "scirc"
            case '\u015E' => "Scedil"
            case '\u015F' => "scedil"
            case '\u0160' => "Scaron"
            case '\u0161' => "scaron"
            case '\u0162' => "Tcedil"
            case '\u0163' => "tcedil"
            case '\u0164' => "Tcaron"
            case '\u0165' => "tcaron"
            case '\u0166' => "Tstrok"
            case '\u0167' => "tstrok"
            case '\u0168' => "Utilde"
            case '\u0169' => "utilde"
            case '\u016A' => "Umacr"
            case '\u016B' => "umacr"
            case '\u016C' => "Ubreve"
            case '\u016D' => "ubreve"
            case '\u016E' => "Uring"
            case '\u016F' => "uring"
            case '\u0170' => "Udblac"
            case '\u0171' => "udblac"
            case '\u0172' => "Uogon"
            case '\u0173' => "uogon"
            case '\u0174' => "Wcirc"
            case '\u0175' => "wcirc"
            case '\u0176' => "Ycirc"
            case '\u0177' => "ycirc"
            case '\u0178' => "Yuml"
            case '\u0179' => "Zacute"
            case '\u017A' => "zacute"
            case '\u017B' => "Zdot"
            case '\u017C' => "zdot"
            case '\u017D' => "Zcaron"
            case '\u017E' => "zcaron"
            case '\u0192' => "fnof"
            case '\u01B5' => "imped"
            case '\u01F5' => "gacute"
            case '\u0237' => "jmath"
            case '\u02C6' => "circ"
            case '\u02C7' => "caron"
            case '\u02D8' => "breve"
            case '\u02D9' => "dot"
            case '\u02DA' => "ring"
            case '\u02DB' => "ogon"
            case '\u02DC' => "tilde"
            case '\u02DD' => "dblac"
            case '\u0311' => "DownBreve"
            case '\u0332' => "UnderBar"
            case '\u0391' => "Alpha"
            case '\u0392' => "Beta"
            case '\u0393' => "Gamma"
            case '\u0394' => "Delta"
            case '\u0395' => "Epsilon"
            case '\u0396' => "Zeta"
            case '\u0397' => "Eta"
            case '\u0398' => "Theta"
            case '\u0399' => "Iota"
            case '\u039A' => "Kappa"
            case '\u039B' => "Lambda"
            case '\u039C' => "Mu"
            case '\u039D' => "Nu"
            case '\u039E' => "Xi"
            case '\u039F' => "Omicron"
            case '\u03A0' => "Pi"
            case '\u03A1' => "Rho"
            case '\u03A3' => "Sigma"
            case '\u03A4' => "Tau"
            case '\u03A5' => "Upsilon"
            case '\u03A6' => "Phi"
            case '\u03A7' => "Chi"
            case '\u03A8' => "Psi"
            case '\u03A9' => "Omega"
            case '\u03B1' => "alpha"
            case '\u03B2' => "beta"
            case '\u03B3' => "gamma"
            case '\u03B4' => "delta"
            case '\u03B5' => "epsiv"
            case '\u03B6' => "zeta"
            case '\u03B7' => "eta"
            case '\u03B8' => "theta"
            case '\u03B9' => "iota"
            case '\u03BA' => "kappa"
            case '\u03BB' => "lambda"
            case '\u03BC' => "mu"
            case '\u03BD' => "nu"
            case '\u03BE' => "xi"
            case '\u03BF' => "omicron"
            case '\u03C0' => "pi"
            case '\u03C1' => "rho"
            case '\u03C2' => "sigmav"
            case '\u03C3' => "sigma"
            case '\u03C4' => "tau"
            case '\u03C5' => "upsi"
            case '\u03C6' => "phi"
            case '\u03C7' => "chi"
            case '\u03C8' => "psi"
            case '\u03C9' => "omega"
            case '\u03D1' => "thetav"
            case '\u03D2' => "Upsi"
            case '\u03D5' => "straightphi"
            case '\u03D6' => "piv"
            case '\u03DC' => "Gammad"
            case '\u03DD' => "gammad"
            case '\u03F0' => "kappav"
            case '\u03F1' => "rhov"
            case '\u03F5' => "epsi"
            case '\u03F6' => "bepsi"
            case '\u0401' => "IOcy"
            case '\u0402' => "DJcy"
            case '\u0403' => "GJcy"
            case '\u0404' => "Jukcy"
            case '\u0405' => "DScy"
            case '\u0406' => "Iukcy"
            case '\u0407' => "YIcy"
            case '\u0408' => "Jsercy"
            case '\u0409' => "LJcy"
            case '\u040A' => "NJcy"
            case '\u040B' => "TSHcy"
            case '\u040C' => "KJcy"
            case '\u040E' => "Ubrcy"
            case '\u040F' => "DZcy"
            case '\u0410' => "Acy"
            case '\u0411' => "Bcy"
            case '\u0412' => "Vcy"
            case '\u0413' => "Gcy"
            case '\u0414' => "Dcy"
            case '\u0415' => "IEcy"
            case '\u0416' => "ZHcy"
            case '\u0417' => "Zcy"
            case '\u0418' => "Icy"
            case '\u0419' => "Jcy"
            case '\u041A' => "Kcy"
            case '\u041B' => "Lcy"
            case '\u041C' => "Mcy"
            case '\u041D' => "Ncy"
            case '\u041E' => "Ocy"
            case '\u041F' => "Pcy"
            case '\u0420' => "Rcy"
            case '\u0421' => "Scy"
            case '\u0422' => "Tcy"
            case '\u0423' => "Ucy"
            case '\u0424' => "Fcy"
            case '\u0425' => "KHcy"
            case '\u0426' => "TScy"
            case '\u0427' => "CHcy"
            case '\u0428' => "SHcy"
            case '\u0429' => "SHCHcy"
            case '\u042A' => "HARDcy"
            case '\u042B' => "Ycy"
            case '\u042C' => "SOFTcy"
            case '\u042D' => "Ecy"
            case '\u042E' => "YUcy"
            case '\u042F' => "YAcy"
            case '\u0430' => "acy"
            case '\u0431' => "bcy"
            case '\u0432' => "vcy"
            case '\u0433' => "gcy"
            case '\u0434' => "dcy"
            case '\u0435' => "iecy"
            case '\u0436' => "zhcy"
            case '\u0437' => "zcy"
            case '\u0438' => "icy"
            case '\u0439' => "jcy"
            case '\u043A' => "kcy"
            case '\u043B' => "lcy"
            case '\u043C' => "mcy"
            case '\u043D' => "ncy"
            case '\u043E' => "ocy"
            case '\u043F' => "pcy"
            case '\u0440' => "rcy"
            case '\u0441' => "scy"
            case '\u0442' => "tcy"
            case '\u0443' => "ucy"
            case '\u0444' => "fcy"
            case '\u0445' => "khcy"
            case '\u0446' => "tscy"
            case '\u0447' => "chcy"
            case '\u0448' => "shcy"
            case '\u0449' => "shchcy"
            case '\u044A' => "hardcy"
            case '\u044B' => "ycy"
            case '\u044C' => "softcy"
            case '\u044D' => "ecy"
            case '\u044E' => "yucy"
            case '\u044F' => "yacy"
            case '\u0451' => "iocy"
            case '\u0452' => "djcy"
            case '\u0453' => "gjcy"
            case '\u0454' => "jukcy"
            case '\u0455' => "dscy"
            case '\u0456' => "iukcy"
            case '\u0457' => "yicy"
            case '\u0458' => "jsercy"
            case '\u0459' => "ljcy"
            case '\u045A' => "njcy"
            case '\u045B' => "tshcy"
            case '\u045C' => "kjcy"
            case '\u045E' => "ubrcy"
            case '\u045F' => "dzcy"
            case '\u2002' => "ensp"
            case '\u2003' => "emsp"
            case '\u2004' => "emsp13"
            case '\u2005' => "emsp14"
            case '\u2007' => "numsp"
            case '\u2008' => "puncsp"
            case '\u2009' => "thinsp"
            case '\u200A' => "hairsp"
            case '\u200B' => "ZeroWidthSpace"
            case '\u200C' => "zwnj"
            case '\u200D' => "zwj"
            case '\u200E' => "lrm"
            case '\u200F' => "rlm"
            case '\u2010' => "hyphen"
            case '\u2013' => "ndash"
            case '\u2014' => "mdash"
            case '\u2015' => "horbar"
            case '\u2016' => "Verbar"
            case '\u2018' => "lsquo"
            case '\u2019' => "rsquo"
            case '\u201A' => "lsquor"
            case '\u201C' => "ldquo"
            case '\u201D' => "rdquo"
            case '\u201E' => "ldquor"
            case '\u2020' => "dagger"
            case '\u2021' => "Dagger"
            case '\u2022' => "bull"
            case '\u2025' => "nldr"
            case '\u2026' => "hellip"
            case '\u2030' => "permil"
            case '\u2031' => "pertenk"
            case '\u2032' => "prime"
            case '\u2033' => "Prime"
            case '\u2034' => "tprime"
            case '\u2035' => "bprime"
            case '\u2039' => "lsaquo"
            case '\u203A' => "rsaquo"
            case '\u203E' => "oline"
            case '\u2041' => "caret"
            case '\u2043' => "hybull"
            case '\u2044' => "frasl"
            case '\u204F' => "bsemi"
            case '\u2057' => "qprime"
            case '\u205F' => "MediumSpace"
            case '\u2060' => "NoBreak"
            case '\u2061' => "ApplyFunction"
            case '\u2062' => "InvisibleTimes"
            case '\u2063' => "InvisibleComma"
            case '\u20AC' => "euro"
            case '\u20DB' => "tdot"
            case '\u20DC' => "DotDot"
            case '\u2102' => "Copf"
            case '\u2105' => "incare"
            case '\u210A' => "gscr"
            case '\u210B' => "hamilt"
            case '\u210C' => "Hfr"
            case '\u210D' => "quaternions"
            case '\u210E' => "planckh"
            case '\u210F' => "planck"
            case '\u2110' => "Iscr"
            case '\u2111' => "image"
            case '\u2112' => "Lscr"
            case '\u2113' => "ell"
            case '\u2115' => "Nopf"
            case '\u2116' => "numero"
            case '\u2117' => "copysr"
            case '\u2118' => "weierp"
            case '\u2119' => "Popf"
            case '\u211A' => "rationals"
            case '\u211B' => "Rscr"
            case '\u211C' => "real"
            case '\u211D' => "reals"
            case '\u211E' => "rx"
            case '\u2122' => "trade"
            case '\u2124' => "integers"
            case '\u2126' => "ohm"
            case '\u2127' => "mho"
            case '\u2128' => "Zfr"
            case '\u2129' => "iiota"
            case '\u212B' => "angst"
            case '\u212C' => "bernou"
            case '\u212D' => "Cfr"
            case '\u212F' => "escr"
            case '\u2130' => "Escr"
            case '\u2131' => "Fscr"
            case '\u2133' => "phmmat"
            case '\u2134' => "order"
            case '\u2135' => "alefsym"
            case '\u2136' => "beth"
            case '\u2137' => "gimel"
            case '\u2138' => "daleth"
            case '\u2145' => "CapitalDifferentialD"
            case '\u2146' => "DifferentialD"
            case '\u2147' => "ExponentialE"
            case '\u2148' => "ImaginaryI"
            case '\u2153' => "frac13"
            case '\u2154' => "frac23"
            case '\u2155' => "frac15"
            case '\u2156' => "frac25"
            case '\u2157' => "frac35"
            case '\u2158' => "frac45"
            case '\u2159' => "frac16"
            case '\u215A' => "frac56"
            case '\u215B' => "frac18"
            case '\u215C' => "frac38"
            case '\u215D' => "frac58"
            case '\u215E' => "frac78"
            case '\u2190' => "larr"
            case '\u2191' => "uarr"
            case '\u2192' => "rarr"
            case '\u2193' => "darr"
            case '\u2194' => "harr"
            case '\u2195' => "varr"
            case '\u2196' => "nwarr"
            case '\u2197' => "nearr"
            case '\u2198' => "searr"
            case '\u2199' => "swarr"
            case '\u219A' => "nlarr"
            case '\u219B' => "nrarr"
            case '\u219D' => "rarrw"
            case '\u219E' => "Larr"
            case '\u219F' => "Uarr"
            case '\u21A0' => "Rarr"
            case '\u21A1' => "Darr"
            case '\u21A2' => "larrtl"
            case '\u21A3' => "rarrtl"
            case '\u21A4' => "LeftTeeArrow"
            case '\u21A5' => "UpTeeArrow"
            case '\u21A6' => "map"
            case '\u21A7' => "DownTeeArrow"
            case '\u21A9' => "larrhk"
            case '\u21AA' => "rarrhk"
            case '\u21AB' => "larrlp"
            case '\u21AC' => "rarrlp"
            case '\u21AD' => "harrw"
            case '\u21AE' => "nharr"
            case '\u21B0' => "lsh"
            case '\u21B1' => "rsh"
            case '\u21B2' => "ldsh"
            case '\u21B3' => "rdsh"
            case '\u21B5' => "crarr"
            case '\u21B6' => "cularr"
            case '\u21B7' => "curarr"
            case '\u21BA' => "olarr"
            case '\u21BB' => "orarr"
            case '\u21BC' => "lharu"
            case '\u21BD' => "lhard"
            case '\u21BE' => "uharr"
            case '\u21BF' => "uharl"
            case '\u21C0' => "rharu"
            case '\u21C1' => "rhard"
            case '\u21C2' => "dharr"
            case '\u21C3' => "dharl"
            case '\u21C4' => "rlarr"
            case '\u21C5' => "udarr"
            case '\u21C6' => "lrarr"
            case '\u21C7' => "llarr"
            case '\u21C8' => "uuarr"
            case '\u21C9' => "rrarr"
            case '\u21CA' => "ddarr"
            case '\u21CB' => "lrhar"
            case '\u21CC' => "rlhar"
            case '\u21CD' => "nlArr"
            case '\u21CE' => "nhArr"
            case '\u21CF' => "nrArr"
            case '\u21D0' => "lArr"
            case '\u21D1' => "uArr"
            case '\u21D2' => "rArr"
            case '\u21D3' => "dArr"
            case '\u21D4' => "hArr"
            case '\u21D5' => "vArr"
            case '\u21D6' => "nwArr"
            case '\u21D7' => "neArr"
            case '\u21D8' => "seArr"
            case '\u21D9' => "swArr"
            case '\u21DA' => "lAarr"
            case '\u21DB' => "rAarr"
            case '\u21DD' => "zigrarr"
            case '\u21E4' => "larrb"
            case '\u21E5' => "rarrb"
            case '\u21F5' => "duarr"
            case '\u21FD' => "loarr"
            case '\u21FE' => "roarr"
            case '\u21FF' => "hoarr"
            case '\u2200' => "forall"
            case '\u2201' => "comp"
            case '\u2202' => "part"
            case '\u2203' => "exist"
            case '\u2204' => "nexist"
            case '\u2205' => "empty"
            case '\u2207' => "nabla"
            case '\u2208' => "isin"
            case '\u2209' => "notin"
            case '\u220B' => "niv"
            case '\u220C' => "notni"
            case '\u220F' => "prod"
            case '\u2210' => "coprod"
            case '\u2211' => "sum"
            case '\u2212' => "minus"
            case '\u2213' => "mnplus"
            case '\u2214' => "plusdo"
            case '\u2216' => "setmn"
            case '\u2217' => "lowast"
            case '\u2218' => "compfn"
            case '\u221A' => "radic"
            case '\u221D' => "prop"
            case '\u221E' => "infin"
            case '\u221F' => "angrt"
            case '\u2220' => "ang"
            case '\u2221' => "angmsd"
            case '\u2222' => "angsph"
            case '\u2223' => "mid"
            case '\u2224' => "nmid"
            case '\u2225' => "par"
            case '\u2226' => "npar"
            case '\u2227' => "and"
            case '\u2228' => "or"
            case '\u2229' => "cap"
            case '\u222A' => "cup"
            case '\u222B' => "int"
            case '\u222C' => "Int"
            case '\u222D' => "tint"
            case '\u222E' => "conint"
            case '\u222F' => "Conint"
            case '\u2230' => "Cconint"
            case '\u2231' => "cwint"
            case '\u2232' => "cwconint"
            case '\u2233' => "awconint"
            case '\u2234' => "there4"
            case '\u2235' => "becaus"
            case '\u2236' => "ratio"
            case '\u2237' => "Colon"
            case '\u2238' => "minusd"
            case '\u223A' => "mDDot"
            case '\u223B' => "homtht"
            case '\u223C' => "sim"
            case '\u223D' => "bsim"
            case '\u223E' => "ac"
            case '\u223F' => "acd"
            case '\u2240' => "wreath"
            case '\u2241' => "nsim"
            case '\u2242' => "esim"
            case '\u2243' => "sime"
            case '\u2244' => "nsime"
            case '\u2245' => "cong"
            case '\u2246' => "simne"
            case '\u2247' => "ncong"
            case '\u2248' => "asymp"
            case '\u2249' => "nap"
            case '\u224A' => "ape"
            case '\u224B' => "apid"
            case '\u224C' => "bcong"
            case '\u224D' => "asympeq"
            case '\u224E' => "bump"
            case '\u224F' => "bumpe"
            case '\u2250' => "esdot"
            case '\u2251' => "eDot"
            case '\u2252' => "efDot"
            case '\u2253' => "erDot"
            case '\u2254' => "colone"
            case '\u2255' => "ecolon"
            case '\u2256' => "ecir"
            case '\u2257' => "cire"
            case '\u2259' => "wedgeq"
            case '\u225A' => "veeeq"
            case '\u225C' => "trie"
            case '\u225F' => "equest"
            case '\u2260' => "ne"
            case '\u2261' => "equiv"
            case '\u2262' => "nequiv"
            case '\u2264' => "le"
            case '\u2265' => "ge"
            case '\u2266' => "lE"
            case '\u2267' => "gE"
            case '\u2268' => "lnE"
            case '\u2269' => "gnE"
            case '\u226A' => "Lt"
            case '\u226B' => "Gt"
            case '\u226C' => "twixt"
            case '\u226D' => "NotCupCap"
            case '\u226E' => "nlt"
            case '\u226F' => "ngt"
            case '\u2270' => "nle"
            case '\u2271' => "nge"
            case '\u2272' => "lsim"
            case '\u2273' => "gsim"
            case '\u2274' => "nlsim"
            case '\u2275' => "ngsim"
            case '\u2276' => "lg"
            case '\u2277' => "gl"
            case '\u2278' => "ntlg"
            case '\u2279' => "ntgl"
            case '\u227A' => "pr"
            case '\u227B' => "sc"
            case '\u227C' => "prcue"
            case '\u227D' => "sccue"
            case '\u227E' => "prsim"
            case '\u227F' => "scsim"
            case '\u2280' => "npr"
            case '\u2281' => "nsc"
            case '\u2282' => "sub"
            case '\u2283' => "sup"
            case '\u2284' => "nsub"
            case '\u2285' => "nsup"
            case '\u2286' => "sube"
            case '\u2287' => "supe"
            case '\u2288' => "nsube"
            case '\u2289' => "nsupe"
            case '\u228A' => "subne"
            case '\u228B' => "supne"
            case '\u228D' => "cupdot"
            case '\u228E' => "uplus"
            case '\u228F' => "sqsub"
            case '\u2290' => "sqsup"
            case '\u2291' => "sqsube"
            case '\u2292' => "sqsupe"
            case '\u2293' => "sqcap"
            case '\u2294' => "sqcup"
            case '\u2295' => "oplus"
            case '\u2296' => "ominus"
            case '\u2297' => "otimes"
            case '\u2298' => "osol"
            case '\u2299' => "odot"
            case '\u229A' => "ocir"
            case '\u229B' => "oast"
            case '\u229D' => "odash"
            case '\u229E' => "plusb"
            case '\u229F' => "minusb"
            case '\u22A0' => "timesb"
            case '\u22A1' => "sdotb"
            case '\u22A2' => "vdash"
            case '\u22A3' => "dashv"
            case '\u22A4' => "top"
            case '\u22A5' => "bottom"
            case '\u22A7' => "models"
            case '\u22A8' => "vDash"
            case '\u22A9' => "Vdash"
            case '\u22AA' => "Vvdash"
            case '\u22AB' => "VDash"
            case '\u22AC' => "nvdash"
            case '\u22AD' => "nvDash"
            case '\u22AE' => "nVdash"
            case '\u22AF' => "nVDash"
            case '\u22B0' => "prurel"
            case '\u22B2' => "vltri"
            case '\u22B3' => "vrtri"
            case '\u22B4' => "ltrie"
            case '\u22B5' => "rtrie"
            case '\u22B6' => "origof"
            case '\u22B7' => "imof"
            case '\u22B8' => "mumap"
            case '\u22B9' => "hercon"
            case '\u22BA' => "intcal"
            case '\u22BB' => "veebar"
            case '\u22BD' => "barvee"
            case '\u22BE' => "angrtvb"
            case '\u22BF' => "lrtri"
            case '\u22C0' => "xwedge"
            case '\u22C1' => "xvee"
            case '\u22C2' => "xcap"
            case '\u22C3' => "xcup"
            case '\u22C4' => "diam"
            case '\u22C5' => "sdot"
            case '\u22C6' => "sstarf"
            case '\u22C7' => "divonx"
            case '\u22C8' => "bowtie"
            case '\u22C9' => "ltimes"
            case '\u22CA' => "rtimes"
            case '\u22CB' => "lthree"
            case '\u22CC' => "rthree"
            case '\u22CD' => "bsime"
            case '\u22CE' => "cuvee"
            case '\u22CF' => "cuwed"
            case '\u22D0' => "Sub"
            case '\u22D1' => "Sup"
            case '\u22D2' => "Cap"
            case '\u22D3' => "Cup"
            case '\u22D4' => "fork"
            case '\u22D5' => "epar"
            case '\u22D6' => "ltdot"
            case '\u22D7' => "gtdot"
            case '\u22D8' => "Ll"
            case '\u22D9' => "Gg"
            case '\u22DA' => "leg"
            case '\u22DB' => "gel"
            case '\u22DE' => "cuepr"
            case '\u22DF' => "cuesc"
            case '\u22E0' => "nprcue"
            case '\u22E1' => "nsccue"
            case '\u22E2' => "nsqsube"
            case '\u22E3' => "nsqsupe"
            case '\u22E6' => "lnsim"
            case '\u22E7' => "gnsim"
            case '\u22E8' => "prnsim"
            case '\u22E9' => "scnsim"
            case '\u22EA' => "nltri"
            case '\u22EB' => "nrtri"
            case '\u22EC' => "nltrie"
            case '\u22ED' => "nrtrie"
            case '\u22EE' => "vellip"
            case '\u22EF' => "ctdot"
            case '\u22F0' => "utdot"
            case '\u22F1' => "dtdot"
            case '\u22F2' => "disin"
            case '\u22F3' => "isinsv"
            case '\u22F4' => "isins"
            case '\u22F5' => "isindot"
            case '\u22F6' => "notinvc"
            case '\u22F7' => "notinvb"
            case '\u22F9' => "isinE"
            case '\u22FA' => "nisd"
            case '\u22FB' => "xnis"
            case '\u22FC' => "nis"
            case '\u22FD' => "notnivc"
            case '\u22FE' => "notnivb"
            case '\u2305' => "barwed"
            case '\u2306' => "Barwed"
            case '\u2308' => "lceil"
            case '\u2309' => "rceil"
            case '\u230A' => "lfloor"
            case '\u230B' => "rfloor"
            case '\u230C' => "drcrop"
            case '\u230D' => "dlcrop"
            case '\u230E' => "urcrop"
            case '\u230F' => "ulcrop"
            case '\u2310' => "bnot"
            case '\u2312' => "profline"
            case '\u2313' => "profsurf"
            case '\u2315' => "telrec"
            case '\u2316' => "target"
            case '\u231C' => "ulcorn"
            case '\u231D' => "urcorn"
            case '\u231E' => "dlcorn"
            case '\u231F' => "drcorn"
            case '\u2322' => "frown"
            case '\u2323' => "smile"
            case '\u232D' => "cylcty"
            case '\u232E' => "profalar"
            case '\u2336' => "topbot"
            case '\u233D' => "ovbar"
            case '\u233F' => "solbar"
            case '\u237C' => "angzarr"
            case '\u23B0' => "lmoust"
            case '\u23B1' => "rmoust"
            case '\u23B4' => "tbrk"
            case '\u23B5' => "bbrk"
            case '\u23B6' => "bbrktbrk"
            case '\u23DC' => "OverParenthesis"
            case '\u23DD' => "UnderParenthesis"
            case '\u23DE' => "OverBrace"
            case '\u23DF' => "UnderBrace"
            case '\u23E2' => "trpezium"
            case '\u23E7' => "elinters"
            case '\u2423' => "blank"
            case '\u24C8' => "oS"
            case '\u2500' => "boxh"
            case '\u2502' => "boxv"
            case '\u250C' => "boxdr"
            case '\u2510' => "boxdl"
            case '\u2514' => "boxur"
            case '\u2518' => "boxul"
            case '\u251C' => "boxvr"
            case '\u2524' => "boxvl"
            case '\u252C' => "boxhd"
            case '\u2534' => "boxhu"
            case '\u253C' => "boxvh"
            case '\u2550' => "boxH"
            case '\u2551' => "boxV"
            case '\u2552' => "boxdR"
            case '\u2553' => "boxDr"
            case '\u2554' => "boxDR"
            case '\u2555' => "boxdL"
            case '\u2556' => "boxDl"
            case '\u2557' => "boxDL"
            case '\u2558' => "boxuR"
            case '\u2559' => "boxUr"
            case '\u255A' => "boxUR"
            case '\u255B' => "boxuL"
            case '\u255C' => "boxUl"
            case '\u255D' => "boxUL"
            case '\u255E' => "boxvR"
            case '\u255F' => "boxVr"
            case '\u2560' => "boxVR"
            case '\u2561' => "boxvL"
            case '\u2562' => "boxVl"
            case '\u2563' => "boxVL"
            case '\u2564' => "boxHd"
            case '\u2565' => "boxhD"
            case '\u2566' => "boxHD"
            case '\u2567' => "boxHu"
            case '\u2568' => "boxhU"
            case '\u2569' => "boxHU"
            case '\u256A' => "boxvH"
            case '\u256B' => "boxVh"
            case '\u256C' => "boxVH"
            case '\u2580' => "uhblk"
            case '\u2584' => "lhblk"
            case '\u2588' => "block"
            case '\u2591' => "blk14"
            case '\u2592' => "blk12"
            case '\u2593' => "blk34"
            case '\u25A1' => "squ"
            case '\u25AA' => "squf"
            case '\u25AB' => "EmptyVerySmallSquare"
            case '\u25AD' => "rect"
            case '\u25AE' => "marker"
            case '\u25B1' => "fltns"
            case '\u25B3' => "xutri"
            case '\u25B4' => "utrif"
            case '\u25B5' => "utri"
            case '\u25B8' => "rtrif"
            case '\u25B9' => "rtri"
            case '\u25BD' => "xdtri"
            case '\u25BE' => "dtrif"
            case '\u25BF' => "dtri"
            case '\u25C2' => "ltrif"
            case '\u25C3' => "ltri"
            case '\u25CA' => "loz"
            case '\u25CB' => "cir"
            case '\u25EC' => "tridot"
            case '\u25EF' => "xcirc"
            case '\u25F8' => "ultri"
            case '\u25F9' => "urtri"
            case '\u25FA' => "lltri"
            case '\u25FB' => "EmptySmallSquare"
            case '\u25FC' => "FilledSmallSquare"
            case '\u2605' => "starf"
            case '\u2606' => "star"
            case '\u260E' => "phone"
            case '\u2640' => "female"
            case '\u2642' => "male"
            case '\u2660' => "spades"
            case '\u2663' => "clubs"
            case '\u2665' => "hearts"
            case '\u2666' => "diams"
            case '\u266A' => "sung"
            case '\u266D' => "flat"
            case '\u266E' => "natur"
            case '\u266F' => "sharp"
            case '\u2713' => "check"
            case '\u2717' => "cross"
            case '\u2720' => "malt"
            case '\u2736' => "sext"
            case '\u2758' => "VerticalSeparator"
            case '\u2772' => "lbbrk"
            case '\u2773' => "rbbrk"
            case '\u27E6' => "lobrk"
            case '\u27E7' => "robrk"
            case '\u27E8' => "lang"
            case '\u27E9' => "rang"
            case '\u27EA' => "Lang"
            case '\u27EB' => "Rang"
            case '\u27EC' => "loang"
            case '\u27ED' => "roang"
            case '\u27F5' => "xlarr"
            case '\u27F6' => "xrarr"
            case '\u27F7' => "xharr"
            case '\u27F8' => "xlArr"
            case '\u27F9' => "xrArr"
            case '\u27FA' => "xhArr"
            case '\u27FC' => "xmap"
            case '\u27FF' => "dzigrarr"
            case '\u2902' => "nvlArr"
            case '\u2903' => "nvrArr"
            case '\u2904' => "nvHarr"
            case '\u2905' => "Map"
            case '\u290C' => "lbarr"
            case '\u290D' => "rbarr"
            case '\u290E' => "lBarr"
            case '\u290F' => "rBarr"
            case '\u2910' => "RBarr"
            case '\u2911' => "DDotrahd"
            case '\u2912' => "UpArrowBar"
            case '\u2913' => "DownArrowBar"
            case '\u2916' => "Rarrtl"
            case '\u2919' => "latail"
            case '\u291A' => "ratail"
            case '\u291B' => "lAtail"
            case '\u291C' => "rAtail"
            case '\u291D' => "larrfs"
            case '\u291E' => "rarrfs"
            case '\u291F' => "larrbfs"
            case '\u2920' => "rarrbfs"
            case '\u2923' => "nwarhk"
            case '\u2924' => "nearhk"
            case '\u2925' => "searhk"
            case '\u2926' => "swarhk"
            case '\u2927' => "nwnear"
            case '\u2928' => "nesear"
            case '\u2929' => "seswar"
            case '\u292A' => "swnwar"
            case '\u2933' => "rarrc"
            case '\u2935' => "cudarrr"
            case '\u2936' => "ldca"
            case '\u2937' => "rdca"
            case '\u2938' => "cudarrl"
            case '\u2939' => "larrpl"
            case '\u293C' => "curarrm"
            case '\u293D' => "cularrp"
            case '\u2945' => "rarrpl"
            case '\u2948' => "harrcir"
            case '\u2949' => "Uarrocir"
            case '\u294A' => "lurdshar"
            case '\u294B' => "ldrushar"
            case '\u294E' => "LeftRightVector"
            case '\u294F' => "RightUpDownVector"
            case '\u2950' => "DownLeftRightVector"
            case '\u2951' => "LeftUpDownVector"
            case '\u2952' => "LeftVectorBar"
            case '\u2953' => "RightVectorBar"
            case '\u2954' => "RightUpVectorBar"
            case '\u2955' => "RightDownVectorBar"
            case '\u2956' => "DownLeftVectorBar"
            case '\u2957' => "DownRightVectorBar"
            case '\u2958' => "LeftUpVectorBar"
            case '\u2959' => "LeftDownVectorBar"
            case '\u295A' => "LeftTeeVector"
            case '\u295B' => "RightTeeVector"
            case '\u295C' => "RightUpTeeVector"
            case '\u295D' => "RightDownTeeVector"
            case '\u295E' => "DownLeftTeeVector"
            case '\u295F' => "DownRightTeeVector"
            case '\u2960' => "LeftUpTeeVector"
            case '\u2961' => "LeftDownTeeVector"
            case '\u2962' => "lHar"
            case '\u2963' => "uHar"
            case '\u2964' => "rHar"
            case '\u2965' => "dHar"
            case '\u2966' => "luruhar"
            case '\u2967' => "ldrdhar"
            case '\u2968' => "ruluhar"
            case '\u2969' => "rdldhar"
            case '\u296A' => "lharul"
            case '\u296B' => "llhard"
            case '\u296C' => "rharul"
            case '\u296D' => "lrhard"
            case '\u296E' => "udhar"
            case '\u296F' => "duhar"
            case '\u2970' => "RoundImplies"
            case '\u2971' => "erarr"
            case '\u2972' => "simrarr"
            case '\u2973' => "larrsim"
            case '\u2974' => "rarrsim"
            case '\u2975' => "rarrap"
            case '\u2976' => "ltlarr"
            case '\u2978' => "gtrarr"
            case '\u2979' => "subrarr"
            case '\u297B' => "suplarr"
            case '\u297C' => "lfisht"
            case '\u297D' => "rfisht"
            case '\u297E' => "ufisht"
            case '\u297F' => "dfisht"
            case '\u2985' => "lopar"
            case '\u2986' => "ropar"
            case '\u298B' => "lbrke"
            case '\u298C' => "rbrke"
            case '\u298D' => "lbrkslu"
            case '\u298E' => "rbrksld"
            case '\u298F' => "lbrksld"
            case '\u2990' => "rbrkslu"
            case '\u2991' => "langd"
            case '\u2992' => "rangd"
            case '\u2993' => "lparlt"
            case '\u2994' => "rpargt"
            case '\u2995' => "gtlPar"
            case '\u2996' => "ltrPar"
            case '\u299A' => "vzigzag"
            case '\u299C' => "vangrt"
            case '\u299D' => "angrtvbd"
            case '\u29A4' => "ange"
            case '\u29A5' => "range"
            case '\u29A6' => "dwangle"
            case '\u29A7' => "uwangle"
            case '\u29A8' => "angmsdaa"
            case '\u29A9' => "angmsdab"
            case '\u29AA' => "angmsdac"
            case '\u29AB' => "angmsdad"
            case '\u29AC' => "angmsdae"
            case '\u29AD' => "angmsdaf"
            case '\u29AE' => "angmsdag"
            case '\u29AF' => "angmsdah"
            case '\u29B0' => "bemptyv"
            case '\u29B1' => "demptyv"
            case '\u29B2' => "cemptyv"
            case '\u29B3' => "raemptyv"
            case '\u29B4' => "laemptyv"
            case '\u29B5' => "ohbar"
            case '\u29B6' => "omid"
            case '\u29B7' => "opar"
            case '\u29B9' => "operp"
            case '\u29BB' => "olcross"
            case '\u29BC' => "odsold"
            case '\u29BE' => "olcir"
            case '\u29BF' => "ofcir"
            case '\u29C0' => "olt"
            case '\u29C1' => "ogt"
            case '\u29C2' => "cirscir"
            case '\u29C3' => "cirE"
            case '\u29C4' => "solb"
            case '\u29C5' => "bsolb"
            case '\u29C9' => "boxbox"
            case '\u29CD' => "trisb"
            case '\u29CE' => "rtriltri"
            case '\u29CF' => "LeftTriangleBar"
            case '\u29D0' => "RightTriangleBar"
            case '\u29DA' => "race"
            case '\u29DC' => "iinfin"
            case '\u29DD' => "infintie"
            case '\u29DE' => "nvinfin"
            case '\u29E3' => "eparsl"
            case '\u29E4' => "smeparsl"
            case '\u29E5' => "eqvparsl"
            case '\u29EB' => "lozf"
            case '\u29F4' => "RuleDelayed"
            case '\u29F6' => "dsol"
            case '\u2A00' => "xodot"
            case '\u2A01' => "xoplus"
            case '\u2A02' => "xotime"
            case '\u2A04' => "xuplus"
            case '\u2A06' => "xsqcup"
            case '\u2A0C' => "qint"
            case '\u2A0D' => "fpartint"
            case '\u2A10' => "cirfnint"
            case '\u2A11' => "awint"
            case '\u2A12' => "rppolint"
            case '\u2A13' => "scpolint"
            case '\u2A14' => "npolint"
            case '\u2A15' => "pointint"
            case '\u2A16' => "quatint"
            case '\u2A17' => "intlarhk"
            case '\u2A22' => "pluscir"
            case '\u2A23' => "plusacir"
            case '\u2A24' => "simplus"
            case '\u2A25' => "plusdu"
            case '\u2A26' => "plussim"
            case '\u2A27' => "plustwo"
            case '\u2A29' => "mcomma"
            case '\u2A2A' => "minusdu"
            case '\u2A2D' => "loplus"
            case '\u2A2E' => "roplus"
            case '\u2A2F' => "Cross"
            case '\u2A30' => "timesd"
            case '\u2A31' => "timesbar"
            case '\u2A33' => "smashp"
            case '\u2A34' => "lotimes"
            case '\u2A35' => "rotimes"
            case '\u2A36' => "otimesas"
            case '\u2A37' => "Otimes"
            case '\u2A38' => "odiv"
            case '\u2A39' => "triplus"
            case '\u2A3A' => "triminus"
            case '\u2A3B' => "tritime"
            case '\u2A3C' => "iprod"
            case '\u2A3F' => "amalg"
            case '\u2A40' => "capdot"
            case '\u2A42' => "ncup"
            case '\u2A43' => "ncap"
            case '\u2A44' => "capand"
            case '\u2A45' => "cupor"
            case '\u2A46' => "cupcap"
            case '\u2A47' => "capcup"
            case '\u2A48' => "cupbrcap"
            case '\u2A49' => "capbrcup"
            case '\u2A4A' => "cupcup"
            case '\u2A4B' => "capcap"
            case '\u2A4C' => "ccups"
            case '\u2A4D' => "ccaps"
            case '\u2A50' => "ccupssm"
            case '\u2A53' => "And"
            case '\u2A54' => "Or"
            case '\u2A55' => "andand"
            case '\u2A56' => "oror"
            case '\u2A57' => "orslope"
            case '\u2A58' => "andslope"
            case '\u2A5A' => "andv"
            case '\u2A5B' => "orv"
            case '\u2A5C' => "andd"
            case '\u2A5D' => "ord"
            case '\u2A5F' => "wedbar"
            case '\u2A66' => "sdote"
            case '\u2A6A' => "simdot"
            case '\u2A6D' => "congdot"
            case '\u2A6E' => "easter"
            case '\u2A6F' => "apacir"
            case '\u2A70' => "apE"
            case '\u2A71' => "eplus"
            case '\u2A72' => "pluse"
            case '\u2A73' => "Esim"
            case '\u2A74' => "Colone"
            case '\u2A75' => "Equal"
            case '\u2A77' => "eDDot"
            case '\u2A78' => "equivDD"
            case '\u2A79' => "ltcir"
            case '\u2A7A' => "gtcir"
            case '\u2A7B' => "ltquest"
            case '\u2A7C' => "gtquest"
            case '\u2A7D' => "les"
            case '\u2A7E' => "ges"
            case '\u2A7F' => "lesdot"
            case '\u2A80' => "gesdot"
            case '\u2A81' => "lesdoto"
            case '\u2A82' => "gesdoto"
            case '\u2A83' => "lesdotor"
            case '\u2A84' => "gesdotol"
            case '\u2A85' => "lap"
            case '\u2A86' => "gap"
            case '\u2A87' => "lne"
            case '\u2A88' => "gne"
            case '\u2A89' => "lnap"
            case '\u2A8A' => "gnap"
            case '\u2A8B' => "lEg"
            case '\u2A8C' => "gEl"
            case '\u2A8D' => "lsime"
            case '\u2A8E' => "gsime"
            case '\u2A8F' => "lsimg"
            case '\u2A90' => "gsiml"
            case '\u2A91' => "lgE"
            case '\u2A92' => "glE"
            case '\u2A93' => "lesges"
            case '\u2A94' => "gesles"
            case '\u2A95' => "els"
            case '\u2A96' => "egs"
            case '\u2A97' => "elsdot"
            case '\u2A98' => "egsdot"
            case '\u2A99' => "el"
            case '\u2A9A' => "eg"
            case '\u2A9D' => "siml"
            case '\u2A9E' => "simg"
            case '\u2A9F' => "simlE"
            case '\u2AA0' => "simgE"
            case '\u2AA1' => "LessLess"
            case '\u2AA2' => "GreaterGreater"
            case '\u2AA4' => "glj"
            case '\u2AA5' => "gla"
            case '\u2AA6' => "ltcc"
            case '\u2AA7' => "gtcc"
            case '\u2AA8' => "lescc"
            case '\u2AA9' => "gescc"
            case '\u2AAA' => "smt"
            case '\u2AAB' => "lat"
            case '\u2AAC' => "smte"
            case '\u2AAD' => "late"
            case '\u2AAE' => "bumpE"
            case '\u2AAF' => "pre"
            case '\u2AB0' => "sce"
            case '\u2AB3' => "prE"
            case '\u2AB4' => "scE"
            case '\u2AB5' => "prnE"
            case '\u2AB6' => "scnE"
            case '\u2AB7' => "prap"
            case '\u2AB8' => "scap"
            case '\u2AB9' => "prnap"
            case '\u2ABA' => "scnap"
            case '\u2ABB' => "Pr"
            case '\u2ABC' => "Sc"
            case '\u2ABD' => "subdot"
            case '\u2ABE' => "supdot"
            case '\u2ABF' => "subplus"
            case '\u2AC0' => "supplus"
            case '\u2AC1' => "submult"
            case '\u2AC2' => "supmult"
            case '\u2AC3' => "subedot"
            case '\u2AC4' => "supedot"
            case '\u2AC5' => "subE"
            case '\u2AC6' => "supE"
            case '\u2AC7' => "subsim"
            case '\u2AC8' => "supsim"
            case '\u2ACB' => "subnE"
            case '\u2ACC' => "supnE"
            case '\u2ACF' => "csub"
            case '\u2AD0' => "csup"
            case '\u2AD1' => "csube"
            case '\u2AD2' => "csupe"
            case '\u2AD3' => "subsup"
            case '\u2AD4' => "supsub"
            case '\u2AD5' => "subsub"
            case '\u2AD6' => "supsup"
            case '\u2AD7' => "suphsub"
            case '\u2AD8' => "supdsub"
            case '\u2AD9' => "forkv"
            case '\u2ADA' => "topfork"
            case '\u2ADB' => "mlcp"
            case '\u2AE4' => "Dashv"
            case '\u2AE6' => "Vdashl"
            case '\u2AE7' => "Barv"
            case '\u2AE8' => "vBar"
            case '\u2AE9' => "vBarv"
            case '\u2AEB' => "Vbar"
            case '\u2AEC' => "Not"
            case '\u2AED' => "bNot"
            case '\u2AEE' => "rnmid"
            case '\u2AEF' => "cirmid"
            case '\u2AF0' => "midcir"
            case '\u2AF1' => "topcir"
            case '\u2AF2' => "nhpar"
            case '\u2AF3' => "parsim"
            case '\u2AFD' => "parsl"
            case '\uFB00' => "fflig"
            case '\uFB01' => "filig"
            case '\uFB02' => "fllig"
            case '\uFB03' => "ffilig"
            case '\uFB04' => "ffllig"
            case _ => s"#${c.toInt}"
          }

        Some(s"&$entity;")
      } )

}