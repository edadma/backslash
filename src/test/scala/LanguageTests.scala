package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest._
import prop.PropertyChecks


class LanguageTests extends FreeSpec with PropertyChecks with Matchers with Testing {

  val today = ZonedDateTime.now.format( DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) )

	"basic" in {
    test( "Hello World!", false ) shouldBe "Hello World!"
    test( """Today is \today .""", false ) shouldBe s"Today is $today."
    test( """3 plus 4 is \+ 3 4 .""", false ) shouldBe "3 plus 4 is 7 ."
    test( """3 plus 4 is \+ 3 4{}.""", false ) shouldBe "3 plus 4 is 7."
    test( """3 plus 4 is \+ 3 {4}.""", false ) shouldBe "3 plus 4 is 7."
    test( """3 plus 4 is {\+ 3 4}.""", false ) shouldBe "3 plus 4 is 7."
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
        |3 plus 4 is \\+ 3 {{4}}.
      """.stripMargin, true ) shouldBe "3 plus 4 is 7."
    test(
      """
        |\delim \\ {{ }}
        |3 plus 4 is {{\\+ 3 4}}.
      """.stripMargin, true ) shouldBe "3 plus 4 is 7."
  }
	
}

//		a [RuntimeException] should be thrownBy {interpret( """ (= 1 1] """ )}
