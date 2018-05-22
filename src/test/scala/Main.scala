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
      |start \if v defined \else undefined end
    """.trim.stripMargin
  val assigns =
    Map(
      "x" -> 3,
      "y" -> 4,
      "l" -> List( Map("name" -> "Larry"), Map("name" -> "Moe"), Map("name" -> "Curly") )
    )
  val parser = new Parser( Command.standard )
  val ast = parser.parse( io.Source.fromString(input) )
  val renderer = new Renderer( parser, config )

//  println( ast )

  val result = renderer.capture( ast, assigns )

//  println( s"|$result|
  println( result )
//  println( result map (_.toInt) )

}