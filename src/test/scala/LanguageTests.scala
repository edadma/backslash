package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest._
import prop.PropertyChecks


class LanguageTests extends FreeSpec with PropertyChecks with Matchers with Testing {
	
	"basic" in {
    test( "Hello World!", false ) shouldBe "Hello World!"
    test( """Today is \today .""", false ) shouldBe s"Today is ${ZonedDateTime.now.format( DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) )}."
    test( """3 plus 4 is \+ 3 {4} .""", false ) shouldBe "3 plus 4 is 7 ."
	}

  "delim" in {

  }
	
}

//		a [RuntimeException] should be thrownBy {interpret( """ (= 1 1] """ )}
