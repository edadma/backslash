package xyz.hyperreal.backslash

import java.time.format.{FormatStyle, DateTimeFormatter}


object Main extends App {

  val config =
    Map(
      'today -> DateTimeFormatter.ofLocalizedDate( FormatStyle.LONG )
    )
  val input = """asdf \date 'yyyy MM dd' sdfg"""
  val parser = new Parser( Command.standard )
  val ast = parser.parse(io.Source.fromString(input))
  val renderer = new Renderer( config, Console.out )
  val result = renderer.render( ast )

}