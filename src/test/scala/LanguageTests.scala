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

  "literals" in {
    test( """asdf \n\t zxvc""", false ) shouldBe "asdf \n\tzxvc"
    test( """asdf \set v '"\b\f\n\r\t\\\'\"'\v zxvc""", false ) shouldBe "asdf \"\b\f\n\r\t\\\'\"zxvc"
    test( """asdf \set v "'\b\f\n\r\t\\\'\""\v zxvc""", false ) shouldBe "asdf '\b\f\n\r\t\\\'\"zxvc"
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
    test( """start \if v defined \else undefined end""", true ) shouldBe "start undefined end"
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
