package xyz.hyperreal

import scala.util.parsing.input.{Position, Reader}


package object backslash {

  def problem( r: Reader[Char], error: String ): Nothing = problem( r.pos, error )

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

}