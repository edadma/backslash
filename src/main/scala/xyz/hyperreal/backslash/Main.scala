package xyz.hyperreal.backslash

import java.io.File

import scala.collection.mutable

import xyz.hyperreal.json.DefaultJSONReader

object Main extends App {

  val config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )
  val assigns = new mutable.HashMap[String, Any]
//  var templateFile: File = _

//  def usage = {
//    println("""
//          |Backslash v0.4.23
//          |
//          |Usage:  java -jar backslash-0.4.23.jar <options> <template>
//          |
//          |Options:  --help              display this help and exit
//          |          -s <name> <string>  assign <string> to variable <name>
//          |          -n <name> <number>  assign <number> to variable <name>
//          |
//          |Note:  <template> may be -- meaning read from standard input
//        """.trim.stripMargin)
//    sys.exit()
//  }

  def json(src: io.Source): Unit =
    for ((k: String, v) <- DefaultJSONReader
           .fromString(src mkString)
           .asInstanceOf[Map[String, Any]])
      assigns(k) = v

//  def run(src: io.Source): Unit = {
//    val parser = new Parser(Command.standard)
//    val renderer = new Renderer(parser, config)
//
//    renderer.render(parser.parse(src), assigns, Console.out)
//  }

  case class Args(strings: Map[String, String],
                  numbers: Map[String, BigDecimal],
                  json: Option[String],
                  input: String,
                  out: Option[File])

  val parser = new scopt.OptionParser[Args]("backslash") {
    head("backslash", "0.4.23")

    help("help").text("prints this usage text")

    opt[Option[String]]('j', "json")
      .valueName("<file/value>")
      .action((x, c) => c.copy(json = x))
      .text("variable assignments as json")

    opt[Map[String, BigDecimal]]('n', "number")
      .valueName("k1=v1, ...")
      .action((x, c) => c.copy(numbers = x))
      .text("numerical variable assignments")

    opt[Option[File]]('o', "out")
      .optional()
      .valueName("<output file>")
      .action((x, c) => c.copy(out = x))
      .validate(x =>
        if (x.get.exists && x.get.canWrite || !x.get.exists && x.get.createNewFile && x.get.canWrite)
          success
        else failure("output file must be writable"))
      .text("output file")

    opt[Map[String, String]]('s', "string")
      .valueName("k1=v1, ...")
      .action((x, c) => c.copy(strings = x))
      .text("string variable assignments")

    arg[String]("<input file>")
      .required()
      .action((x, c) => c.copy(input = x))
      .validate(x =>
        if (x == "--") success
        else {
          val f = new File(x)

          if (f.exists && f.isFile && f.canRead) success
          else failure("input file must exist and be readable")
      })
      .text("input file or -- for stdin")
  }

  parser.parse(args, Args(Map(), Map(), None, null, None)) match {
    case Some(a) =>
      println(a)
//      run(io.Source.fromFile(templateFile))
    case None =>
  }

}
