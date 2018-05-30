package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest._
import prop.PropertyChecks


class LanguageTests extends FreeSpec with PropertyChecks with Matchers with Testing {

  val today = ZonedDateTime.now.format( DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) )

	"basic" in {
    test( "", false ) shouldBe ""
    test( "Hello World!", false ) shouldBe "Hello World!"
    test( """Today is \today .""", false ) shouldBe s"Today is $today."
    test( """3 plus 4 is \+ 3 4 .""", false ) shouldBe "3 plus 4 is 7 ."
    test( """3 plus 4 is \+ 3 4{}.""", false ) shouldBe "3 plus 4 is 7."
    test( """3 plus 4 is {\+ 3 4}.""", false ) shouldBe "3 plus 4 is 7."
	}

  "seq" in {
    test( """\seq {1 2 3}""", false ) shouldBe "[1, 2, 3]"
    test( """\seq {1 2 3} | map \+ _ 3""", false ) shouldBe "[4, 5, 6]"
    test( """\set v 4\seq {1 2 3 \v}""", false ) shouldBe "[1, 2, 3, 4]"
    test( """\seq {}""", false ) shouldBe "[]"
  }

  "obj" in {
    test( """\{three 3 four 4 five 5}""", false ) shouldBe """{"three": 3, "four": 4, "five": 5}"""
    test( """\set v 6\{three 3 four 4 five 5 six \v}""", false ) shouldBe """{"three": 3, "four": 4, "five": 5, "six": 6}"""
    test( """\{}""", false ) shouldBe """{}"""
  }

  "literals" in {
    test( """asdf \n\t zxvc""", false ) shouldBe "asdf \n\tzxvc"
    test( """asdf \set v '"\b\f\n\r\t\\\'\"'\v zxvc""", false ) shouldBe "asdf \"\b\f\n\r\t\\\'\"zxvc"
    test( """asdf \set v "'\b\f\n\r\t\\\'\""\v zxvc""", false ) shouldBe "asdf '\b\f\n\r\t\\\'\"zxvc"
    test( """asdf \set v 123.4\+ \v 1 zxvc""", false ) shouldBe "asdf 124.4 zxvc"
    a [RuntimeException] should be thrownBy {test( """\split "a b" \null""", false )}
    test( """\null""", false ) shouldBe "null"
    a [RuntimeException] should be thrownBy {test( """\split "a b" \true""", false )}
    test( """\true""", false ) shouldBe "true"
    a [RuntimeException] should be thrownBy {test( """\split "a b" \false""", false )}
    test( """\false""", false ) shouldBe "false"
    a [RuntimeException] should be thrownBy {test( """asdf \set v "'\b\f\n\r\t\\\'\"""", false )}
  }

  "if" in {
    test( """start \if v defined end""", true ) shouldBe "start end"
    test( """start \if v defined end""", true, "v" -> 123 ) shouldBe "start defined end"
    test( """start \if v defined \elsif x x end""", true ) shouldBe "start end"
    test( """start \if v defined \elsif x x end""", true, "x" -> 123 ) shouldBe "start x end"
    test( """start \if v defined \elsif x x end""", true, "v" -> 123 ) shouldBe "start defined end"
    test( """start \if v defined \elsif x x \else undefined end""", true ) shouldBe "start undefined end"
    test( """start \if v defined \elsif x x \else undefined end""", true, "x" -> 123 ) shouldBe "start x end"
    test( """start \if v defined \elsif x x \else undefined end""", true, "v" -> 123 ) shouldBe "start defined end"
    test( """start \if v defined \else undefined end""", true ) shouldBe "start undefined end"
    test( """start \if v defined \else undefined end""", true, "v" -> 123 ) shouldBe "start defined end"
  }

  "unless" in {
    test( "start \\unless v undefined end", true ) shouldBe "start undefined end"
    test( "start \\unless v undefined end", true, "v" -> 123 ) shouldBe "start end"
    test( "start \\unless v undefined \\else defined end", true ) shouldBe "start undefined end"
    test( "start \\unless v undefined \\else defined end", true, "v" -> 123 ) shouldBe "start defined end"
  }

  "for" in {
    test( """start \for l \_i end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start abcend"
    test( """start \for l {\_i} end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start abc end"
    test( """start \for l {\_i\ } end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a b c end"
    test( """start \for l {\_i\ } \else else end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a b c else end"
    test( """start \for m \f end""", true, "m" -> Map("f" -> 123) ) shouldBe
      "start 123end"
  }

  "filters" in {
    test( """\l | \reverse | \take 2 | \drop 1""", true, "l" -> List("a", "b", "c") ) shouldBe
      """["b"]"""
    test( """\l | map \+ _ 1 | filter \> _ 5""", true, "l" -> List[BigDecimal](3, 4, 5, 6, 7) ) shouldBe
      """[6, 7, 8]"""
    test( """\seq {\{a 3} \{b 4} \{a 5}} | map a | filter _""", false ) shouldBe "[3, 5]"
  }

  "break" in {
    test( """start \for l {\if \> \_idx 1 \break \_i\ } \else else end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a b end"
    test( """start \for l {\if \> \_idx 1 \break {\_i\ }} end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a b end"
  }

  "continue" in {
    test( """start \for l {\if \= \_idx 1 \continue \_i\ } \else else end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a c else end"
    test( """start \for l {\if \= \_idx 1 \continue {\_i\ }} end""", true, "l" -> List("a", "b", "c") ) shouldBe
      "start a c end"
  }

  "connectives" in {
    test( """start {\and \true \true} end""", true ) shouldBe
      "start true end"
    test( """start {\and \true \false} end""", true ) shouldBe
      "start false end"
    test( """start {\and \true v} end""", true, "v" -> 123 ) shouldBe
      "start true end"
    test( """start {\and \true v} end""", true ) shouldBe
      "start false end"
    test( """start {\or \true \true} end""", true ) shouldBe
      "start true end"
    test( """start {\or \true \false} end""", true ) shouldBe
      "start true end"
    test( """start {\or v \false} end""", true ) shouldBe
      "start false end"
    test( """start {\or v \false} end""", true, "v" -> 123 ) shouldBe
      "start 123 end"
    test( """start {\not \true} end""", true ) shouldBe
      "start false end"
    test( """start {\not v} end""", true ) shouldBe
      "start true end"
    test( """start {\not v} end""", true, "v" -> 123 ) shouldBe
      "start false end"
  }

  "set" in {
    test( """\set v {have a nice day}\v""", true ) shouldBe
      "have a nice day"
    test( """\set v \+ 3 4\v""", true ) shouldBe
      "7"
  }

  "match" in {
    test( """start \match 5 end""", true ) shouldBe
      "start end"
    test( """start \match 5 \case 3 three end""", true ) shouldBe
      "start end"
    test( """start \match 5 \case 3 three \else else end""", true ) shouldBe
      "start else end"
    test( """start \match 5 \case 5 five end""", true ) shouldBe
      "start five end"
    test( """start \match 5 \case 3 three \case 5 five end""", true ) shouldBe
      "start five end"
    test( """start \match 5 \case 5 five \case 3 three end""", true ) shouldBe
      "start five end"
    test( """start \match 5 \case 5 five \else else end""", true ) shouldBe
      "start five end"
  }

  "comments" in {
    test( """3 plus \#super boring\#4 is {\+ 3 4}.""", false ) shouldBe "3 plus 4 is 7."
    test( """3 plus\# super boring \# 4 is {\+ 3 4}.""", false ) shouldBe "3 plus 4 is 7."
    test(
      """
        |\#
        |  This is the start of a very boring comment.
        |
        |  This is the end of a very boring comment.
        |\#
        |
        |Today is \today .
      """.stripMargin, true ) shouldBe s"Today is $today."
    a [RuntimeException] should be thrownBy {test( """3 plus \#super boring""", false )}
  }

  "raw" in {
    test(
      """
        |\<<<
        |\set v 'asdf'
        |\v
        |\>>> wow
      """.stripMargin
      , false ) shouldBe
      """
        |\set v 'asdf'
        |\v
        |wow
      """.stripMargin
  }

  "delim" in {
    test(
      """
        |\delim \\ {{ }}
        |Today is \today .
      """.stripMargin, true ) shouldBe s"Today is \\today ."
    test(
      """
        |\delim \\ {{ }}
        |3 plus 4 is \\+ 3 4 .
      """.stripMargin, true ) shouldBe "3 plus 4 is 7 ."
    test(
      """
        |\delim \\ {{ }}
        |3 plus 4 is \\+ 3 4{{}}.
      """.stripMargin, true ) shouldBe "3 plus 4 is 7."
    test(
      """
        |\delim \\ {{ }}
        |3 plus 4 is {{\\+ 3 4}}.
      """.stripMargin, true ) shouldBe "3 plus 4 is 7."
  }
	
}
