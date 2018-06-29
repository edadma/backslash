package xyz.hyperreal.backslash


object Main extends App {

  val config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )
  val input =
    """
      |\delim \ [ ]
      |    \set entity [rgb(73, 172, 206)]
      |    \set tag [rgb(148, 176, 94)]
      |    \set string [rgb(81, 167, 182)]
      |    \set \lum 15
      |    <html>
      |        <head>
      |            <title>HTML highlighting example</title>
      |            <style>
      |                .highlight .hll { background-color: rgb(12, 11, 10); }
      |                .highlight  { background: #272822; color: rgb(140, 131, 126); }
      |                .highlight .entitydelim { color: \lighten\lum\entity; }
      |                .highlight .entity { color: \entity; }
      |                .highlight .preproc { color: #75715e }
      |                .highlight .comment { color: rgb(69, 64, 61); }
      |                .highlight .tagdelim { color: \lighten\lum\tag; }
      |                .highlight .tag { color: \tag; }
      |                .highlight .attr { color: rgb(98, 200, 172); }
      |                .highlight .oper { color: rgb(238, 171, 106); }
      |                .highlight .stringdelim { color: \lighten\lum\string; }
      |                .highlight .string { color: \string; }
      |            </style>
      |        </head>
      |        <body>
      |            <div class="highlight"><pre>\content</pre></div>
      |        </body>
      |    </html>
    """.trim.stripMargin
  val assigns =
    Map(
      "l" -> List[BigDecimal]( 3, 4, 5, 6, 7 ),
      "x" -> 3,
      "y" -> 4,
      "m" -> Map( "firstName" -> "Bugs", "lastName" -> "Bunny" ),
      "products" -> List( Map("name" -> "RCA 32\u2033 ROKU SMART TV", "price" -> BigDecimal("207.00"), "inStock" -> true), Map("asdf" -> "asdf"), Map("name" -> "LG 55UK6300", "price" -> BigDecimal("1098.00"), "inStock" -> false) )
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
