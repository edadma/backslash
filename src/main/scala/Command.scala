package xyz.hyperreal.backslash

import java.io.PrintStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import collection.mutable


abstract class Command( val name: String, var arity: Int ) extends ((Map[Symbol, Any], mutable.Map[String, Any], PrintStream, List[Any], AnyRef) => Unit) {
  override def toString = s"<$name>"
}

object Command {

  val standard =
    List(
      new Command( "today", 0 ) {
        def apply( config: Map[Symbol, Any], vars: mutable.Map[String, Any], out: PrintStream, args: List[Any], context: AnyRef ): Unit = {
          out.print( ZonedDateTime.now.format(config('today).asInstanceOf[DateTimeFormatter]) )
        }
      }
    ) map (c => c.name -> c) toMap

}