package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import collection.mutable


abstract class Command( val name: String, var arity: Int ) extends ((Map[Symbol, Any], mutable.Map[String, Any], List[Any], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

  val standard =
    List(

      new Command( "today", 0 ) {
        def apply( config: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( config('today).asInstanceOf[DateTimeFormatter] )
      },

      new Command( "date", 1 ) {
        def apply( config: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( DateTimeFormatter.ofPattern(args.head.toString) )
      }

    ) map (c => c.name -> c) toMap

}