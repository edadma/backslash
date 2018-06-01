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

  "addition" in {
    test( """\+ 3 4""", true ) shouldBe "7"
    test( """\+ 3 4""", true ) shouldBe "7"
    a [RuntimeException] should be thrownBy {test( """\+ asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\+ 1 asdf""", false )}
  }

  "multiplication" in {
    test( """\* 3 4""", true ) shouldBe "12"
    a [RuntimeException] should be thrownBy {test( """\* asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\* 1 asdf""", false )}
  }

  "subtraction" in {
    test( """\- 3 4""", true ) shouldBe "-1"
    a [RuntimeException] should be thrownBy {test( """\- asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\- 1 asdf""", false )}
  }

  "range" in {
    test( """\.. 3 6""", true ) shouldBe "[3, 4, 5, 6]"
    test( """\.. 3.1 6""", true ) shouldBe "[3.1, 4.1, 5.1]"
    test( """\.. 3 2""", true ) shouldBe "[]"
    a [RuntimeException] should be thrownBy {test( """\.. 3 asdf""", false )}
    a [RuntimeException] should be thrownBy {test( """\.. asdf 3""", false )}
  }

  "division" in {
    test( """\/ 3 4""", true ) shouldBe "0.75"
    a [RuntimeException] should be thrownBy {test( """\/ asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\/ 1 asdf""", false )}
  }

  "unequal" in {
    test( """\/= 3 4""", true ) shouldBe "true"
  }

  "less-than" in {
    test( """\< 3 4""", true ) shouldBe "true"
    test( """\< a b""", true ) shouldBe "true"
    test( """\< ac b""", true ) shouldBe "true"
    test( """\< 3 3""", true ) shouldBe "false"
    test( """\< a a""", true ) shouldBe "false"
    a [RuntimeException] should be thrownBy {test( """\< asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\< 1 asdf""", false )}
  }

  "less-than-or-equal" in {
    test( """\<= 3 4""", true ) shouldBe "true"
    test( """\<= a b""", true ) shouldBe "true"
    test( """\<= ac b""", true ) shouldBe "true"
    test( """\<= 3 3""", true ) shouldBe "true"
    test( """\<= a a""", true ) shouldBe "true"
    a [RuntimeException] should be thrownBy {test( """\<= asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\<= 1 asdf""", false )}
  }

  "equal" in {
    test( """\= 3 4""", true ) shouldBe "false"
  }

  "greater-than" in {
    test( """\> 3 4""", true ) shouldBe "false"
    test( """\> a b""", true ) shouldBe "false"
    test( """\> ac b""", true ) shouldBe "false"
    test( """\> 3 3""", true ) shouldBe "false"
    test( """\> a a""", true ) shouldBe "false"
    a [RuntimeException] should be thrownBy {test( """\> asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\> 1 asdf""", false )}
  }

  "greater-than-or-equal" in {
    test( """\>= 3 4""", true ) shouldBe "false"
    test( """\>= a b""", true ) shouldBe "false"
    test( """\>= ac b""", true ) shouldBe "false"
    test( """\>= 3 3""", true ) shouldBe "true"
    test( """\>= a a""", true ) shouldBe "true"
    a [RuntimeException] should be thrownBy {test( """\>= asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\>= 1 asdf""", false )}
  }

  "empty-sequence" in {
    test( """\[]""", true ) shouldBe "[]"
  }

  "power" in {
    test( """\^ 3 4""", true ) shouldBe "81"
    test( """\^ 3 -4""", true ) shouldBe "0.01234567901234567901234567901234568"
    a [RuntimeException] should be thrownBy {test( """\^ asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\^ 1 asdf""", false )}
  }

}