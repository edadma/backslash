//@
package xyz.hyperreal.backslash

import java.io.{ByteArrayOutputStream, PrintStream}
import java.time.format.{DateTimeFormatter, FormatStyle}


trait Testing {

  val config =
    Map(
      'today -> DateTimeFormatter.ofLocalizedDate( FormatStyle.LONG ),
      'include -> "."
    )

	def test( input: String, collapse: Boolean, assigns: (String, Any)* ) = {
		val bytes = new ByteArrayOutputStream
    val parser = new Parser( Command.standard )
    val ast = parser.parse( io.Source.fromString(input) )
    val renderer = new Renderer( parser, config )

    renderer.render( ast, assigns toMap, new PrintStream(bytes) )

		if (collapse)
			bytes.toString.trim.replaceAll( """\s+""", " " )
		else
			bytes.toString
	}

}
