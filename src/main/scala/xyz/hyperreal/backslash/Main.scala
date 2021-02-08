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
  var templateFile: File = _

  def usage = {
    println("""
          |Backslash v0.4.23
          |
          |Usage:  java -jar backslash-0.4.23.jar <options> <template>
          |
          |Options:  --help              display this help and exit
          |          -s <name> <string>  assign <string> to variable <name>
          |          -n <name> <number>  assign <number> to variable <name>
          |
          |Note:  <template> may be -- meaning read from standard input
        """.trim.stripMargin)
    sys.exit
  }

  def json(src: io.Source) =
    for ((k: String, v) <- DefaultJSONReader
           .fromString(src mkString)
           .asInstanceOf[Map[String, Any]])
      assigns(k) = v

  def run(src: io.Source): Unit = {
    val parser = new Parser(Command.standard)
    val renderer = new Renderer(parser, config)

    renderer.render(parser.parse(src), assigns, Console.out)
  }

  if (args isEmpty)
    usage

  case class Args(strings: Map[String, String],
                  numbers: Map[String, BigDecimal],
                  json: Option[String],
                  input: String,
                  out: Option[File])

  val parser = new scopt.OptionParser[Args]("backslash") {
    head("backslash", "0.4.23")

    help("help").text("prints this usage text")

    opt[Option[String]]('j', "json")
      .action((x, c) => c.copy(json = x))
      .text("foo is an integer property")

    opt[Map[String, BigDecimal]]("n")
      .valueName("k1=v1, k2=v2, ...")
      .action((x, c) => c.copy(numbers = x))
      .text("other arguments")

    opt[Map[String, String]]("s")
      .valueName("k1=v1, k2=v2, ...")
      .action((x, c) => c.copy(strings = x))
      .text("other arguments")

    arg[String]("<input file>...")
      .required()
      .action((x, c) => c.copy(input = x))
      .validate(x =>
        if (isReadable(x)) success
        else failure("input file must exist and be readable"))
      .text("input file or -- for stdin")

    opt[Option[File]]('o', "out")
      .optional()
      .valueName("<output file>")
      .action((x, c) => c.copy(out = x))
      .validate(x =>
        if (isReadable(x)) success
        else failure("output file must exist and be readable"))
      .text("output file")

  }
  Options(args) {
    templateFile = new File(file)

    if (templateFile.exists && templateFile.isFile && templateFile.canRead) {
      run(io.Source.fromFile(templateFile))
      Nil
    } else
      sys.error(s"error reading file: $file")
  }

}
