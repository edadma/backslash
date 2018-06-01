package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest._
import prop.PropertyChecks


class CommandTests extends FreeSpec with PropertyChecks with Matchers with Testing {

  val today = ZonedDateTime.now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

  "number" in {
    test( """\set int "3."\set frac "5"\+ 1 \number \+ \int \frac""", false ) shouldBe "4.5"
    a [RuntimeException] should be thrownBy {test( """\number "asdf"""", false )}
  }

  "markdown" in {
    test( """\markdown {this is a __boring__ *test*}""", false ) shouldBe "<p>this is a <strong>boring</strong> <em>test</em></p>"
  }

  "ceil" in {
    test( """\+ 0 \ceil 3""", true ) shouldBe "3"
    test( """\ceil 3.1""", true ) shouldBe "4"
    test( """\ceil -3.1""", true ) shouldBe "-3"
  }

  "floor" in {
    test( """\+ 0 \floor 3""", true ) shouldBe "3"
    test( """\floor 3.1""", true ) shouldBe "3"
    test( """\floor -3.1""", true ) shouldBe "-4"
  }
}