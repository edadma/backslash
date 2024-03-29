package io.github.edadma.backslash

import io.github.edadma.cross_platform.{readFile, processArgs, readableFile, writableFile}
import io.github.edadma.datetime.{DatetimeFormatter, Timezone}

import scala.collection.mutable
import io.github.edadma.json.DefaultJSONReader

object Main extends App {

  val config =
    Map(
      "today" -> DatetimeFormatter("MMMM D, Y"),
      "timezone" -> Timezone.UTC,
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )

  val parser = new scopt.OptionParser[Args]("backslash") {
    head("backslash", "0.4.24")
    help("help").text("prints this usage text")
    opt[Option[String]]('j', "json")
      .valueName("<file/value>")
      .action((x, c) => c.copy(json = x))
      .text("key/value assignments from JSON")
    opt[Map[String, BigDecimal]]('n', "number")
      .valueName("k1=v1, ...")
      .action((x, c) => c.copy(numbers = x))
      .text("numerical key/value assignments")
    opt[Option[String]]('o', "out")
      .valueName("<output file>")
      .action((x, c) => c.copy(out = x))
      .validate(
        x =>
          if (writableFile(x.get))
            success
          else failure("output file must be writable"))
      .text("output file")
    opt[Map[String, String]]('s', "string")
      .valueName("k1=v1, ...")
      .action((x, c) => c.copy(strings = x))
      .text("string key/value assignments")
    arg[String]("<input file>")
      .required()
      .action((x, c) => c.copy(input = x))
      .validate(
        x =>
          if (x == "--") success
          else if (readableFile(x)) success
          else failure("input file must exist and be readable"))
      .text("input file or -- for stdin")
  }

  parser.parse(processArgs(args), Args(Map(), Map(), None, null, None)) match {
    case Some(Args(strings, numbers, json, input, out)) =>
      val parser = new Parser(Command.standard)
      val renderer = new Renderer(parser, config)
      val os =
        out match {
          case None => Console.out
//          case Some(f) => new PrintStream(new FileOutputStream(f))//todo: stdout
        }
      val assigns = new mutable.HashMap[String, Any]

      if (json.isDefined)
        for ((k: String, v) <- DefaultJSONReader
               .fromString(if (json.get startsWith "{") json.get
               else readFile(json.get))
               .asInstanceOf[Map[String, Any]])
          assigns(k) = v

      val src =
        if (input.trim == "--") "" //todo: io.Source.stdin
        else readFile(input)

      renderer.render(parser.parse(src), assigns ++ strings ++ numbers, os)
    case None =>
  }

  case class Args(strings: Map[String, String],
                  numbers: Map[String, BigDecimal],
                  json: Option[String],
                  input: String,
                  out: Option[String])

}
