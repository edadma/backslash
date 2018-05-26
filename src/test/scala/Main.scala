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
      |<h3>Products</h3>
      |
      |<ul>
      |  \for products {
      |    <li>\name&emsp;$\price&emsp;
      |      \if inStock {
      |        <a href="#">Buy It!</a>
      |      } \else {
      |        Out of stock.
      |      }
      |    </li>
      |  }
      |</ul>
    """.trim.stripMargin
  val assigns =
    Map(
      "x" -> 3,
      "y" -> 4,
      "m" -> Map( "firstName" -> "Bugs", "lastName" -> "Bunny" ),
      "products" -> List( Map("name" -> "RCA 32\u2033 ROKU SMART TV", "price" -> BigDecimal("207.00"), "inStock" -> true), Map("name" -> "LG 55UK6300", "price" -> BigDecimal("1098.00"), "inStock" -> false) )
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