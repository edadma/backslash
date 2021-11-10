package io.github.edadma.backslash

import io.github.edadma.datetime.{DatetimeFormatter, Timezone}

trait Testing {

  val config =
    Map(
      "today" -> DatetimeFormatter("MMMM D, Y"),
      "timezone" -> Timezone.UTC,
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )

  def test(input: String, collapse: Boolean, assigns: (String, Any)*) = {
    val parser = new Parser(Command.standard)
    val ast = parser.parse(input)
    val renderer = new Renderer(parser, config)
    val result = renderer.capture(ast, assigns toMap)

    if (collapse)
      result.trim.replaceAll("""\s+""", " ")
    else
      result
  }

}
