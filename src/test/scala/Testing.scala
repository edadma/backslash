//@
package xyz.hyperreal.backslash


trait Testing {

  val config =
    Map(
      "today" -> "MMM d, y",
      "include" -> "."
    )

	def test( input: String, collapse: Boolean, assigns: (String, Any)* ) = {
    val parser = new Parser( Command.standard )
    val ast = parser.parse( io.Source.fromString(input) )
    val renderer = new Renderer( parser, config )
    val result = renderer.capture( ast, assigns toMap )

		if (collapse)
			result.trim.replaceAll( """\s+""", " " )
		else
			result
	}

}
