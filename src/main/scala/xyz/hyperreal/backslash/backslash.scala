package xyz.hyperreal

import xyz.hyperreal.char_reader.CharReader

import scala.util.matching.Regex

package object backslash {

  val numberRegex: Regex = """-?\d+(\.\d+)?|0x[0-9a-fA-F]+""".r

  def problem(r: CharReader, error: String): Nothing =
    if (r eq null)
      sys.error(error)
    else
      r.error(error)

  case object nil {
    override def toString = ""
  }

  def isNumber(a: String): Boolean = numberRegex.pattern.matcher(a).matches

  def number(a: Any): Option[BigDecimal] =
    a match {
      case s: String if isNumber(s) =>
        if (s startsWith "0x")
          Some(BigDecimal(Integer.parseInt(s substring 2, 16)))
        else
          Some(BigDecimal(s))
      case n: BigDecimal => Some(n)
      case _             => None
    }

  def round(n: BigDecimal, scale: Int, config: Map[String, Any]): BigDecimal =
    n.setScale(scale,
               BigDecimal.RoundingMode.withName(config("rounding").toString))

  def truthy(a: Any): Boolean = !falsy(a)

  def falsy(a: Any): Boolean = a == nil || a == false

  def display(a: Any): String =
    a match {
      case m: collection.Map[_, _] =>
        m map { case (k, v) => qdisplay(k) + ": " + qdisplay(v) } mkString ("{", ", ", "}")
      case l: collection.Seq[_] => l map qdisplay mkString ("[", ", ", "]")
      case s                    => String.valueOf(s)
    }

  def qdisplay(a: Any): String =
    a match {
      case s: String => s""""$s""""
      case true      => "<true>"
      case false     => "<false>"
      case null      => "<null>"
      case `nil`     => "<nil>"
      case _         => display(a)
    }

}
