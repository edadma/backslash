package xyz.hyperreal.backslash

import java.time.format.{FormatStyle, DateTimeFormatter}


object Main extends App {

  val config =
    Map(
      'today -> DateTimeFormatter.ofLocalizedDate( FormatStyle.LONG ),
      'include -> "."
    )
  val input =
    """
      |\def asdf {asdf asdf}
      |
      |<em>\today</em>
      |
      |\for \in item \l {
      |  \if \< \_idx 2
      |    \asdf
      |
      |  <p>Hi \item\ \+ 3 4 !</p>
      |}
    """.stripMargin
  val assigns =
    Map(
      "x" -> 3,
      "y" -> 4,
      "l" -> List( Map("name" -> "Larry"), Map("name" -> "Moe"), Map("name" -> "Curly") )
    )
  val parser = new Parser( Command.standard )
  val ast = parser.parse( io.Source.fromString(input) )
  val renderer = new Renderer( parser, config )

  renderer.render( ast, assigns, Console.out )
  println

}