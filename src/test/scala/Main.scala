package xyz.hyperreal.backslash

import java.time.format.{FormatStyle, DateTimeFormatter}


object Main extends App {

  val config =
    Map(
      'today -> DateTimeFormatter.ofLocalizedDate( FormatStyle.LONG )
    )
  val input = """asdf \if v wow zxcv"""
  val assigns =
    Map(
      "v" -> 123
    )
  val parser = new Parser( Command.standard )
  val ast = parser.parse(io.Source.fromString(input))
  val renderer = new Renderer( config )

  renderer.render( ast, assigns, Console.out )
  println

}