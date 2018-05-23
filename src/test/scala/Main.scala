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
      |\for m {
      |  {\firstName} \lastName
      |}
      |
      |\firstName
      |\m
    """.trim.stripMargin
  val assigns =
    Map(
      "x" -> 3,
      "y" -> 4,
      "m" -> Map( "firstName" -> "Bugs", "lastName" -> "Bunny" ),
      "l" -> List( Map("name" -> "Larry"), Map("name" -> "Moe"), Map("name" -> "Curly") )
    )
  val parser = new Parser( Command.standard )
  val ast = parser.parse( io.Source.fromString(input) )
  val renderer = new Renderer( parser, config )

//  println( ast )

  val result = renderer.capture( ast, assigns )

//  println( s"|$result|
  println( result.trim )
//  println( result map (_.toInt) )

}