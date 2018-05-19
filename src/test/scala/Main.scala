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
      |<em>\today</em>
      |
      |\for l {
      |  \if \< \idx 2
      |    wow
      |
      |  <p>Hi \i\ \+ 3 4 !</p>
      |}
    """.stripMargin
  val assigns =
    Map(
      "x" -> 3,
      "y" -> 4,
      "l" -> List( "Larry", "Moe", "Curly" )
    )
  val parser = new Parser( Command.standard )
  val ast = parser.parse( io.Source.fromString(input) )
  val renderer = new Renderer( parser, config )

  renderer.render( ast, assigns, Console.out )
  println

}