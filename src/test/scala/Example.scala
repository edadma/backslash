import scala.util.parsing.input.Position

import xyz.hyperreal.backslash._


object Example extends App {

  val input =
    """
      |<h2>Vaudeville Acts</h2>
      |<ol>
      |  \for \in act \acts {
      |    <li>
      |      <h3>\act.name</h3>
      |      \list \act.members
      |    </li>
      |  }
      |</ol>
    """.trim.stripMargin
  val acts =
    List(
      Map(
        "name" -> "Three Stooges",
        "members" -> List( "Larry", "Moe", "Curly" )
      ),
      Map(
        "name" -> "Andrews Sisters",
        "members" -> List( "LaVerne", "Maxine", "Patty" )
      ),
      Map(
        "name" -> "Abbott and Costello",
        "members" -> List( "William (Bud) Abbott", "Lou Costello" )
      )
    )
  val customCommand =
    new Command( "list", 1 ) {
      def apply( pos: Position, rendered: Renderer, args: List[Any], context: AnyRef ) = {
        val list = args.head.asInstanceOf[List[String]]

        s"<ul>${list map (item => s"<li>$item</li>") mkString}</ul>"
      }
    }

  val parser = new Parser( Command.standard ++ Map("list" -> customCommand) )
  val renderer = new Renderer( parser, Map() )

  renderer.render( parser.parse(io.Source.fromString(input)), Map("acts" -> acts), Console.out )
}