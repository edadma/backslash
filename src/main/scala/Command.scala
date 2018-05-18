package xyz.hyperreal.backslash

import java.time.{OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import collection.mutable
import scala.util.parsing.input.Position


abstract class Command( val name: String, var arity: Int ) extends ((Position, Map[Symbol, Any], mutable.Map[String, Any], List[Any], AnyRef) => Any) {
  override def toString = s"<$name/$arity>"
}

object Command {

  val standard =
    List(

      new Command( "today", 0 ) {
        def apply( pos: Position, config: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          ZonedDateTime.now.format( config('today).asInstanceOf[DateTimeFormatter] )
      },

      new Command( "now", 0 ) {
        def apply( pos: Position, config: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          OffsetDateTime.now
      },

      new Command( "date", 2 ) {
        def apply( pos: Position, config: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          args match {
            case List( date: TemporalAccessor, format: String ) => DateTimeFormatter.ofPattern( format ).format( date )
            case List( a, b ) => problem( pos, s"expected arguments <date> <format>, given $a $b" )
          }
      },

      new Command( "nil", 1 ) {
        def apply( pos: Position, onfig: Map[Symbol, Any], vars: mutable.Map[String, Any], args: List[Any], context: AnyRef ): Any =
          nil
      }

    ) map (c => c.name -> c) toMap

}