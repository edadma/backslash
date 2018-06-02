package xyz.hyperreal.backslash

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
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

  "abs" in {
    test( """\abs 4""", true ) shouldBe "4"
    test( """\abs -4""", true ) shouldBe "4"
    a [RuntimeException] should be thrownBy {test( """\abs asdf""", false )}
  }

  "append" in {
    test( """\append ample ex""", true ) shouldBe "example"
    test( """\append 3 \seq {1 2}""", true ) shouldBe "[1, 2, 3]"
    a [RuntimeException] should be thrownBy {test( """\append asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\append 1 asdf""", false )}
  }

  "ceil" in {
    test( """\+ 0 \ceil 3""", true ) shouldBe "3"
    test( """\ceil 3.1""", true ) shouldBe "4"
    test( """\ceil -3.1""", true ) shouldBe "-3"
  }

  "contains" in {
    test( """\contains {the truth is out there} truth""", true ) shouldBe "true"
    test( """\contains \seq {1 2 3} 3""", true ) shouldBe "true"
    test( """\contains \{three 3 four 4 five 5} four""", true ) shouldBe "true"
    test( """\contains {the truth is out there} asdf""", true ) shouldBe "false"
    test( """\contains \seq {1 2 3} 4""", true ) shouldBe "false"
    test( """\contains \{three 3 four 4 five 5} asdf""", true ) shouldBe "false"
    a [RuntimeException] should be thrownBy {test( """\contains asdf 1""", false )}
    a [RuntimeException] should be thrownBy {test( """\contains 1 asdf""", false )}
  }

  "date" in {
    val date = LocalDate.parse( "20111203", DateTimeFormatter.BASIC_ISO_DATE )

    test( """\date "MMMM d, y" \d""", true, "d" -> date ) shouldBe "December 3, 2011"
    a [RuntimeException] should be thrownBy {test( """\date asdf 1""", false )}
  }

  "default" in {
    test( """\default 3 \v""", true ) shouldBe "3"
    test( """\default 3 \v""", true, "v" -> 4 ) shouldBe "4"
  }

  "distinct" in {
    test( """\seq {1 2 3 2 4 3 5} | distinct""", true ) shouldBe "[1, 2, 3, 4, 5]"
    a [RuntimeException] should be thrownBy {test( """\distinct asdf""", false )}
  }

  "downcase" in {
    test( """\downcase {Hello World!}""", true ) shouldBe "hello world!"
    a [RuntimeException] should be thrownBy {test( """\downcase 123""", false )}
  }

  "drop" in {
    test( """\drop 2 \seq {3 4 5 6 7}""", true ) shouldBe "[5, 6, 7]"
    test( """\drop 2 asdf""", true ) shouldBe "df"
    a [RuntimeException] should be thrownBy {test( """\drop asdf 123""", false )}
    a [RuntimeException] should be thrownBy {test( """\drop 123 123""", false )}
  }

}