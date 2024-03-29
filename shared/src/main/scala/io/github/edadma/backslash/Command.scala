package io.github.edadma.backslash

import io.github.edadma.char_reader.CharReader
import io.github.edadma.cross_platform.{nameSeparator, readFile}
import io.github.edadma.datetime
import io.github.edadma.datetime.{Datetime, DatetimeFormatter, Timezone}

import java.util.regex.Matcher
import io.github.edadma.hsl.HSL

import scala.util.matching.Regex

abstract class Command(val name: String, val arity: Int, val eval: Boolean = true)
    extends ((CharReader, Renderer, List[Any], Map[String, Any], AnyRef) => Any) {
  override def toString =
    s"""io.github.edadma.backslash.Command.standard("$name")"""
}

object Command {

  val hslRegex: Regex =
    """hsl\(\s*(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)\s*%\s*,\s*(\d+(?:\.\d+)?)\s*%\s*\)""" r
  val hslaRegex: Regex =
    """hsla\(\s*(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)\s*%\s*,\s*(\d+(?:\.\d+)?)\s*%\s*,\s*(\d+(?:\.\d+)?)\s*\)""" r
  val rgbRegex: Regex = """rgb\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)""" r
  val rgbaRegex: Regex =
    """rgba\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+(?:\.\d+)?)\s*\)""" r
  val colorRegex: Regex = "#([0-9a-fA-F]{6})".r

  class Const[T] {
    private var set = false
    private var value: T = _

    def apply(v: => T): T = {
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

  def invoke(renderer: Renderer, lambda: AST, arg: Any): Any = {
    renderer.enterScope()
    renderer.scopes.top("_") = arg

    val res = renderer.eval(lambda)

    renderer.exitScope()
    res
  }

  val escapeRegex: Regex = """([^\w _.,!:;?-])""" r

  val standard: Map[String, Command] =
    List(
      new Command(" ", 0) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = " "
      },
      new Command("*", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a * b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("+", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a + b
            case List(a: collection.Map[_, _], b: collection.Map[_, _]) =>
              (a ++ b) toMap
            case List(a: Seq[_], b: Seq[_]) => a ++ b
            case List(a: Seq[_], b: Any)    => a :+ b
            case List(a: Any, b: Seq[_])    => a +: b
            case List(a: String, b: String) => a + b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number> or <string> <string>: $a, $b")
          }
      },
      new Command("-", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a - b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("/", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a / b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("/=", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args.head != args.tail.head
      },
      new Command("<", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: String, b: String)         => a < b
            case List(a: BigDecimal, b: BigDecimal) => a < b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number> or <string> <string>: $a, $b")
          }
      },
      new Command("<=", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: String, b: String)         => a <= b
            case List(a: BigDecimal, b: BigDecimal) => a <= b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number> or <string> <string>: $a, $b")
          }
      },
      new Command("=", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args.head == args.tail.head
      },
      new Command(">", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: String, b: String)         => a > b
            case List(a: BigDecimal, b: BigDecimal) => a > b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number> or <string> <string>: $a, $b")
          }
      },
      new Command(">=", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: String, b: String)         => a >= b
            case List(a: BigDecimal, b: BigDecimal) => a >= b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number> or <string> <string>: $a, $b")
          }
      },
      new Command("[]", 0) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          Nil
      },
      new Command("^", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) if b.isValidInt =>
              a pow b.intValue
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <integer>: $a, $b")
          }
      },
      new Command("abs", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case n: BigDecimal => n.abs
            case a             => problem(pos, s"not a number: $a")
          }
        }
      },
      new Command("append", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: Any, b: Seq[_])    => b :+ a
            case List(a: String, b: String) => b + a
            case List(a, b) =>
              problem(pos, s"expected arguments <any> <sequence> or <string> <string>: $a, $b")
          }
      },
      new Command("ceil", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case n: BigDecimal => n.setScale(0, BigDecimal.RoundingMode.CEILING)
            case a             => problem(pos, s"not a number: $a")
          }
        }
      },
      new Command("contains", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: String, b: String) => a contains b
            case List(a: Seq[_], b)         => a contains b
            case List(a: Map[_, _], b) =>
              a.asInstanceOf[Map[Any, Any]] contains b
            case List(a, b) =>
              problem(pos, s"expected arguments <string> <string> or <sequence> <any> or <object> <any>: $a, $b")
          }
      },
      new Command("darken", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(v: Number, c: String) =>
              c match {
                case rgbRegex(r, g, b) =>
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l - v.intValue / 100.0).toRGB

                  s"rgb($nr, $ng, $nb)"
                case rgbaRegex(r, g, b, a) =>
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l - v.intValue / 100.0).toRGB

                  s"rgba($nr, $ng, $nb, $a)"
                case hslRegex(h, s, l) =>
                  val hsl =
                    HSL(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
                  val HSL(hue, sat, lum) =
                    hsl.luminosity(hsl.l - v.intValue / 100.0)

                  f"hsl(${hue * 360}%.1f, ${sat * 100}%.1f%%, ${lum * 100}%.1f%%)"
                case hslaRegex(h, s, l, a) =>
                  val hsl =
                    HSL(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
                  val HSL(hue, sat, lum) =
                    hsl.luminosity(hsl.l - v.intValue / 100.0)

                  f"hsla(${hue * 360}%.1f, ${sat * 100}%.1f%%, ${lum * 100}%.1f%%, ${a.toDouble}%.3f)"
                case colorRegex(hex) =>
                  val List(r: Int, g: Int, b: Int) =
                    hex grouped 2 map (Integer.parseInt(_, 16)) toList
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l - v.intValue / 100.0).toRGB

                  f"#$nr%02x$ng%02x$nb%02x"
                case _ => sys.error(s"color doesn't match known format: $c")
              }
            case List(c: String, _: Number) =>
              sys.error(s"color doesn't match known format: $c")
            case List(a, b) =>
              problem(pos, s"expected <string> <number> arguments: $a, $b")
          }
      },
      new Command("date", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(format: String, date: Datetime) => DatetimeFormatter(format).format(date)
            case List(a, b)                           => problem(pos, s"expected arguments <format> <date>, given $a, $b")
          }
      },
      new Command("default", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: Any, b: Any) => if (b == nil) a else b
          }
      },
      new Command("distinct", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: Seq[_]) => s distinct
            case List(a)         => problem(pos, s"expected sequence argument: $a")
          }
      },
      new Command("downcase", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s toLowerCase
            case List(a)         => problem(pos, s"expected string argument: $a")
          }
      },
      new Command("drop", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(n: BigDecimal, s: Seq[_]) if n.isValidInt =>
              s drop n.toInt
            case List(n: BigDecimal, s: String) if n.isValidInt =>
              s drop n.toInt
            case List(a, b) =>
              problem(pos, s"expected arguments <integer> <sequence> or <integer> <string>, given $a, $b")
          }
      },
      new Command("escape", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case s: String => escape(s)
            case a         => problem(pos, s"not a string: $a")
          }
        }
      },
      new Command("escapeFull", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case s: String => escapeFull(s)
            case a         => problem(pos, s"not a string: $a")
          }
        }
      },
      new Command("escapeOnce", 1) {
        val regex: Regex = """&#?\w+;""" r

        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case s: String =>
              val it = regex.findAllIn(s)
              var last = 0
              val buf = new StringBuilder

              while (it hasNext) {
                it.next()
                buf ++= escape(s.substring(last, it.start))
                buf ++= it.matched
                last = it.end
              }

              buf ++= escape(s.substring(last, s.length))
              buf.toString
            case a => problem(pos, s"not a string: $a")
          }
        }
      },
      new Command("filter", 2, false) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          (args.head, renderer eval args.tail.head.asInstanceOf[AST]) match {
            case (lambda: AST, s: Seq[_]) =>
              s filter (e => truthy(invoke(renderer, lambda, e)))
            case (a, b) =>
              problem(pos, s"expected arguments <lambda <sequence>, given $a, $b")
          }
      },
      new Command("floor", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case n: BigDecimal => n.setScale(0, BigDecimal.RoundingMode.FLOOR)
            case a             => problem(pos, s"not a number: $a")
          }
        }
      },
      new Command("head", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s head
            case List(s: Seq[_]) => s head
            case List(a) =>
              problem(pos, s"expected string or sequence argument: $a")
          }
      },
      new Command("include", 1) {
        val dir = new Const[String]

        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          val file = s"${dir(renderer.config("include").toString)}$nameSeparator${args.head.toString}"
          val charset = optional get "charset" map (_.toString)

          renderer.eval(
            renderer.parser.parse(
            /*if (charset.isDefined)*/ readFile(file))) //todo: charset
        }
      },
      new Command("isEmpty", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case s: String               => s isEmpty
            case m: collection.Map[_, _] => m isEmpty
            case s: Seq[_]               => s isEmpty
            case a =>
              problem(pos, s"expected string, map or sequence argument: $a")
          }
        }
      },
      new Command("join", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(sep: String, s: Seq[_]) => s mkString sep
            case List(a, b) =>
              problem(pos, s"expected arguments <separator> <sequence>, given $a, $b")
          }
      },
      new Command("last", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s last
            case List(s: Seq[_]) => s last
            case List(a) =>
              problem(pos, s"expected string or sequence argument: $a")
          }
      },
      new Command("lighten", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(v: Number, c: String) =>
              c match {
                case rgbRegex(r, g, b) =>
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l + v.intValue / 100.0).toRGB

                  s"rgb($nr, $ng, $nb)"
                case rgbaRegex(r, g, b, a) =>
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l + v.intValue / 100.0).toRGB

                  s"rgba($nr, $ng, $nb, $a)"
                case hslRegex(h, s, l) =>
                  val hsl =
                    HSL(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
                  val HSL(hue, sat, lum) =
                    hsl.luminosity(hsl.l + v.intValue / 100.0)

                  f"hsl(${hue * 360}%.1f, ${sat * 100}%.1f%%, ${lum * 100}%.1f%%)"
                case hslaRegex(h, s, l, a) =>
                  val hsl =
                    HSL(h.toDouble / 360, s.toDouble / 100, l.toDouble / 100)
                  val HSL(hue, sat, lum) =
                    hsl.luminosity(hsl.l + v.intValue / 100.0)

                  f"hsla(${hue * 360}%.1f, ${sat * 100}%.1f%%, ${lum * 100}%.1f%%, ${a.toDouble}%.3f)"
                case colorRegex(hex) =>
                  val List(r: Int, g: Int, b: Int) =
                    hex grouped 2 map (Integer.parseInt(_, 16)) toList
                  val hsl = HSL.fromRGB(r.toInt, g.toInt, b.toInt)
                  val (nr, ng, nb) =
                    hsl.luminosity(hsl.l + v.intValue / 100.0).toRGB

                  f"#$nr%02x$ng%02x$nb%02x"
                case _ => sys.error(s"color doesn't match known format: $c")
              }
            case List(c: String, _: Number) =>
              sys.error(s"color doesn't match known format: $c")
            case List(a, b) =>
              problem(pos, s"expected <string> <number> arguments: $a, $b")
          }
      },
      new Command("lit", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args.head
      },
      new Command("map", 2, false) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          (args.head, renderer eval args.tail.head.asInstanceOf[AST]) match {
            case (LiteralAST(f: String), s: Seq[_]) =>
              s.asInstanceOf[Seq[Map[String, Any]]] map (_ getOrElse (f, nil))
            case (lambda: AST, s: Seq[_]) => s map (invoke(renderer, lambda, _))
            case (a, b) =>
              problem(pos, s"expected arguments <variable> <sequence> or <lambda <sequence>>, given $a, $b")
          }
      },
//      new Command( "markdown", 1 ) {
//        def apply( pos: CharReader, renderer: Renderer, args: List[Any], optional: Map[String, Any], context: AnyRef ): Any =
//          Markdown( args.head.toString )
//      },
      new Command("max", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a max b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("min", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a min b
            case List(a, b) =>
              problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
//      new Command( "n", 0 ) {
//        def apply( pos: CharReader, renderer: Renderer, args: List[Any], optional: Map[String, Any], context: AnyRef ): Any =
//          "\n"
//      },
      new Command("negate", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case n: BigDecimal => -n
            case a             => problem(pos, s"not a number: $a")
          }
        }
      },
      new Command("nil", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          nil
        }
      },
      new Command("nonEmpty", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args.head match {
            case s: String               => s nonEmpty
            case m: collection.Map[_, _] => m nonEmpty
            case s: Seq[_]               => s nonEmpty
            case a =>
              problem(pos, s"expected string, map or sequence argument: $a")
          }
        }
      },
      new Command("normalize", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args.head.toString.trim.replaceAll("""\s+""", " ")
      },
      new Command("now", 0) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = Datetime.now(renderer.config("timezone").asInstanceOf[Timezone])
      },
      new Command("range", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(start: BigDecimal, end: BigDecimal) => start to end by 1
            case List(a, b)                               => problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("rem", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(a: BigDecimal, b: BigDecimal) => a remainder b
            case List(a, b)                         => problem(pos, s"expected arguments <number> <number>: $a, $b")
          }
      },
      new Command("remove", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(l: String, r: String) => r replace (l, "")
            case List(a, b)                 => problem(pos, s"expected arguments <string> <string>: $a, $b")
          }
      },
      new Command("removeFirst", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(l: String, r: String) =>
              r replaceFirst (Matcher.quoteReplacement(l), "")
            case List(a, b) => problem(pos, s"expected arguments <string> <string>: $a, $b")
          }
      },
      new Command("replace", 3) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(l1: String, l2: String, r: String) => r.replace(l1, l2)
            case List(a, b, c)                           => problem(pos, s"expected arguments <string> <string> <string>: $a, $b, $c")
          }
      },
      new Command("replaceFirst", 3) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(l1: String, l2: String, r: String) =>
              r replaceFirst (Matcher.quoteReplacement(l1), l2)
            case List(a, b, c) => problem(pos, s"expected arguments <string> <string> <string>: $a, $b, $c")
          }
      },
      new Command("reverse", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s reverse
            case List(s: Seq[_]) => s reverse
            case List(a)         => problem(pos, s"expected string or sequence argument: $a")
          }
      },
      new Command("round", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          (args.head, optional.getOrElse("scale", 0)) match {
            case (n: BigDecimal, scale: Number) => round(n, scale.intValue, renderer.config)
            case (a, b)                         => problem(pos, s"not a number: $a, $b")
          }
        }
      },
      new Command("size", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String)               => s length
            case List(s: Seq[_])               => s length
            case List(s: collection.Map[_, _]) => s size
            case List(a) =>
              problem(pos, s"expected string or sequence argument: $a")
          }
      },
      new Command("slice", 3) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          args match {
            case List(start: BigDecimal, end: BigDecimal, s: String) if start.isValidInt && end.isValidInt =>
              s.slice(start.intValue, end.intValue)
            case List(start: BigDecimal, end: BigDecimal, s: Seq[_]) if start.isValidInt && end.isValidInt =>
              s.slice(start.intValue, end.intValue)
            case List(a, b, c) =>
              problem(pos, s"expected arguments <start> <end> <string> or <start> <end> <sequence>: $a, $b, $c")
          }
        }
      },
      new Command("sort", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          val on = optional get "on" map (_.toString)
          val desc = (optional get "order" map (_.toString)) contains "desc"

          def comp(a: Any, b: Any) = if (desc) !lt(a, b) else lt(a, b)

          def lt(a: Any, b: Any) =
            (a, b) match {
              case (a: Comparable[_], b: Comparable[_]) =>
                (a.asInstanceOf[Comparable[Any]] compareTo b) < 0
              case _ => a.toString < b.toString
            }

          args match {
            case List(s: Seq[_]) if on isDefined =>
              s.asInstanceOf[Seq[Map[String, Any]]] sortWith ((a, b) => comp(a(on.get), b(on.get)))
            case List(s: Seq[_]) => s sortWith comp
            case List(a)         => problem(pos, s"expected sequence argument: $a")
          }
        }
      },
      new Command("split", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(sep: String, s: String) =>
              s split sep toVector
            case List(a, b) => problem(pos, s"expected arguments <string> <string>, given $a, $b")
          }
      },
//      new Command( "t", 0 ) {
//        def apply( pos: CharReader, renderer: Renderer, args: List[Any], optional: Map[String, Any], context: AnyRef ): Any =
//          "\t"
//      },
      new Command("tail", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s tail
            case List(s: Seq[_]) => s tail
            case List(a)         => problem(pos, s"expected string or sequence argument: $a")
          }
      },
      new Command("take", 2) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(n: BigDecimal, s: Seq[_]) if n.isValidInt => s take n.toInt
            case List(n: BigDecimal, s: String) if n.isValidInt => s take n.toInt
            case List(a, b) =>
              problem(pos, s"expected arguments <integer> <sequence> or <integer> <string>, given $a, $b")
          }
      },
      new Command("timestamp", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String)                                => Datetime.fromString(s).timestamp
            case List(millis: BigDecimal) if millis isValidLong => Datetime.fromMillis(millis.longValue).timestamp
            case List(a)                                        => problem(pos, s"expected string or integer argument: $a")
          }
      },
      new Command("toInteger", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          val x = args.head

          number(x) match {
            case None                => problem(pos, s"not a number: $x")
            case Some(n: BigDecimal) => if (n.isWhole) n else n.setScale(0, BigDecimal.RoundingMode.DOWN)
          }
        }
      },
      new Command("toNumber", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any = {
          val x = args.head

          number(x) match {
            case None    => problem(pos, s"not a number: $x")
            case Some(n) => n
          }
        }
      },
      new Command("toString", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          display(args.head)
      },
      new Command("today", 0) {
        val format = new Const[DatetimeFormatter] //todo: not sure, check against Liquid

        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          renderer
            .config("today")
            .asInstanceOf[datetime.DatetimeFormatter]
            .format(Datetime.now(renderer.config("timezone").asInstanceOf[Timezone]))
      },
      new Command("trim", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s trim
            case List(a)         => problem(pos, s"expected string argument: $a")
          }
      },
      new Command("u", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(n: BigDecimal) if n.isValidChar => n.toChar
            case List(n: BigDecimal) =>
              problem(pos, s"number not a valid character: $n")
            case List(a) => problem(pos, s"expected number argument, given $a")
          }
      },
      new Command("upcase", 1) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          args match {
            case List(s: String) => s toUpperCase
            case List(a)         => problem(pos, s"expected string argument: $a")
          }
      },
      new Command("{}", 0) {
        def apply(pos: CharReader,
                  renderer: Renderer,
                  args: List[Any],
                  optional: Map[String, Any],
                  context: AnyRef): Any =
          Map()
      },
    ) map (c => c.name -> c) toMap

  def escape(s: String) =
    escapeRegex.replaceSomeIn(s, { m =>
      Entity(m group 1 head) map (e => s"&$e;")
    })

  def escapeFull(s: String) =
    escapeRegex.replaceAllIn(s, { m =>
      s"&${Entity.full(m group 1 head)};"
    })

}
