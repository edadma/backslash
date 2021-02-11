//@
package xyz.hyperreal.backslash

import java.time.{LocalDate, ZonedDateTime}
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CommandTests extends AnyFreeSpec with Matchers with Testing {

  val today = ZonedDateTime.now.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

  "space" in {
    test("""
        |\set firstName John \set lastName Doe
        |\firstName\ \lastName
      """.stripMargin,
         true) shouldBe "John Doe"
  }

  "addition" in {
    test("""\+ 3 4""", true) shouldBe "7"
    test("""\+ as df""", true) shouldBe "asdf"
    test("""\+ \seq {3 4} \seq {5 6}""", true) shouldBe "[3, 4, 5, 6]"
    test("""\+ \{a 3 b 4} \{c 5 d 6}""", true) shouldBe """{"a": 3, "b": 4, "c": 5, "d": 6}"""
    test("""\+ \seq {3 4} 5""", true) shouldBe "[3, 4, 5]"
    test("""\+ 4 \seq {5 6}""", true) shouldBe "[4, 5, 6]"
    a[RuntimeException] should be thrownBy { test("""\+ asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\+ 1 asdf""", false) }
  }

  "multiplication" in {
    test("""\* 3 4""", true) shouldBe "12"
    a[RuntimeException] should be thrownBy { test("""\* asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\* 1 asdf""", false) }
  }

  "subtraction" in {
    test("""\- 3 4""", true) shouldBe "-1"
    a[RuntimeException] should be thrownBy { test("""\- asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\- 1 asdf""", false) }
  }

  "division" in {
    test("""\/ 3 4""", true) shouldBe "0.75"
    a[RuntimeException] should be thrownBy { test("""\/ asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\/ 1 asdf""", false) }
  }

  "unequal" in {
    test("""\/= 3 4""", true) shouldBe "true"
  }

  "less-than" in {
    test("""\< 3 4""", true) shouldBe "true"
    test("""\< a b""", true) shouldBe "true"
    test("""\< ac b""", true) shouldBe "true"
    test("""\< 3 3""", true) shouldBe "false"
    test("""\< a a""", true) shouldBe "false"
    a[RuntimeException] should be thrownBy { test("""\< asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\< 1 asdf""", false) }
  }

  "less-than-or-equal" in {
    test("""\<= 3 4""", true) shouldBe "true"
    test("""\<= a b""", true) shouldBe "true"
    test("""\<= ac b""", true) shouldBe "true"
    test("""\<= 3 3""", true) shouldBe "true"
    test("""\<= a a""", true) shouldBe "true"
    a[RuntimeException] should be thrownBy { test("""\<= asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\<= 1 asdf""", false) }
  }

  "equal" in {
    test("""\= 3 4""", true) shouldBe "false"
  }

  "greater-than" in {
    test("""\> 3 4""", true) shouldBe "false"
    test("""\> a b""", true) shouldBe "false"
    test("""\> ac b""", true) shouldBe "false"
    test("""\> 3 3""", true) shouldBe "false"
    test("""\> a a""", true) shouldBe "false"
    a[RuntimeException] should be thrownBy { test("""\> asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\> 1 asdf""", false) }
  }

  "greater-than-or-equal" in {
    test("""\>= 3 4""", true) shouldBe "false"
    test("""\>= a b""", true) shouldBe "false"
    test("""\>= ac b""", true) shouldBe "false"
    test("""\>= 3 3""", true) shouldBe "true"
    test("""\>= a a""", true) shouldBe "true"
    a[RuntimeException] should be thrownBy { test("""\>= asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\>= 1 asdf""", false) }
  }

  "empty-sequence" in {
    test("""\[]""", true) shouldBe "[]"
  }

  "power" in {
    test("""\^ 3 4""", true) shouldBe "81"
    test("""\^ 3 -4""", true) shouldBe "0.01234567901234567901234567901234568"
    a[RuntimeException] should be thrownBy { test("""\^ asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\^ 1 asdf""", false) }
  }

  "abs" in {
    test("""\abs 4""", true) shouldBe "4"
    test("""\abs -4""", true) shouldBe "4"
    a[RuntimeException] should be thrownBy { test("""\abs asdf""", false) }
  }

  "append" in {
    test("""\append ample ex""", true) shouldBe "example"
    test("""\append 3 \seq {1 2}""", true) shouldBe "[1, 2, 3]"
    a[RuntimeException] should be thrownBy { test("""\append asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\append 1 asdf""", false) }
  }

  "ceil" in {
    test("""\+ 0 \ceil 3""", true) shouldBe "3"
    test("""\ceil 3.1""", true) shouldBe "4"
    test("""\ceil -3.1""", true) shouldBe "-3"
    a[RuntimeException] should be thrownBy { test("""\ceil asdf""", false) }
  }

  "contains" in {
    test("""\contains {the truth is out there} truth""", true) shouldBe "true"
    test("""\contains \seq {1 2 3} 3""", true) shouldBe "true"
    test("""\contains \{three 3 four 4 five 5} four""", true) shouldBe "true"
    test("""\contains {the truth is out there} asdf""", true) shouldBe "false"
    test("""\contains \seq {1 2 3} 4""", true) shouldBe "false"
    test("""\contains \{three 3 four 4 five 5} asdf""", true) shouldBe "false"
    a[RuntimeException] should be thrownBy {
      test("""\contains asdf 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\contains 1 asdf""", false)
    }
  }

  "date" in {
    val date = LocalDate.parse("20111203", DateTimeFormatter.BASIC_ISO_DATE)

    test("""\date "MMMM d, y" \d""", true, "d" -> date) shouldBe "December 3, 2011"
    a[RuntimeException] should be thrownBy { test("""\date asdf 1""", false) }
  }

  "default" in {
    test("""\default 3 \v""", true) shouldBe "3"
    test("""\default 3 \v""", true, "v" -> 4) shouldBe "4"
  }

  "distinct" in {
    test("""\seq {1 2 3 2 4 3 5} | distinct""", true) shouldBe "[1, 2, 3, 4, 5]"
    a[RuntimeException] should be thrownBy { test("""\distinct asdf""", false) }
  }

  "downcase" in {
    test("""\downcase {Hello World!}""", true) shouldBe "hello world!"
    a[RuntimeException] should be thrownBy { test("""\downcase 123""", false) }
  }

  "drop" in {
    test("""\drop 2 \seq {3 4 5 6 7}""", true) shouldBe "[5, 6, 7]"
    test("""\drop 2 asdf""", true) shouldBe "df"
    a[RuntimeException] should be thrownBy { test("""\drop asdf 123""", false) }
    a[RuntimeException] should be thrownBy { test("""\drop 123 123""", false) }
  }

  "escape" in {
    test("""\escape {a < b}""", true) shouldBe "a &lt; b"
    a[RuntimeException] should be thrownBy { test("""\escape 123""", false) }
  }

  "escapeOnce" in {
    test("""\escapeOnce {a < b &lt; c}""", true) shouldBe "a &lt; b &lt; c"
    a[RuntimeException] should be thrownBy {
      test("""\escapeOnce 123""", false)
    }
  }

  "filter" in {
    test("""\seq {1 2 3 4} | filter \> _ 2""", true) shouldBe "[3, 4]"
    a[RuntimeException] should be thrownBy {
      test("""\filter 123 123""", false)
    }
  }

  "floor" in {
    test("""\+ 0 \floor 3""", true) shouldBe "3"
    test("""\floor 3.1""", true) shouldBe "3"
    test("""\floor -3.1""", true) shouldBe "-4"
    a[RuntimeException] should be thrownBy { test("""\floor asdf""", false) }
  }

  "head" in {
    test("""\seq {3 4 5} | head""", true) shouldBe "3"
    test("""\head asdf""", true) shouldBe "a"
    a[RuntimeException] should be thrownBy { test("""\head 123""", false) }
    a[RuntimeException] should be thrownBy { test("""\head \[]""", false) }
  }

  "join" in {
    test("""\join " - " \seq {1 2 3}""", true) shouldBe "1 - 2 - 3"
    a[RuntimeException] should be thrownBy {
      test("""\join asdf asdf""", false)
    }
  }

  "last" in {
    test("""\seq {3 4 5} | last""", true) shouldBe "5"
    test("""\last asdf""", true) shouldBe "f"
    a[RuntimeException] should be thrownBy { test("""\last 123""", false) }
    a[RuntimeException] should be thrownBy { test("""\last \[]""", false) }
  }

  "lit" in {
    test("""\lit -123 | abs""", true) shouldBe "123"
    test("""\lit {this is a test} | replace 'is' '**'""", true) shouldBe "th** ** a test"
  }

  "map" in {
    test("""\seq {3 4 5} | map \+ _ 2""", true) shouldBe "[5, 6, 7]"
    a[RuntimeException] should be thrownBy { test("""\map 123 123""", false) }
  }

//  "markdown" in {
//    test( """\markdown {this is a __boring__ *test*}""", false ) shouldBe "<p>this is a <strong>boring</strong> <em>test</em></p>"
//  }

  "max" in {
    test("""\max 3 4""", true) shouldBe "4"
    a[RuntimeException] should be thrownBy { test("""\max asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\max 1 asdf""", false) }
  }

  "min" in {
    test("""\min 3 4""", true) shouldBe "3"
    a[RuntimeException] should be thrownBy { test("""\min asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\min 1 asdf""", false) }
  }

//  "newline" in {
//    test( """\n""", false ) shouldBe "\n"
//  }

  "negate" in {
    test("""\negate 4""", true) shouldBe "-4"
    test("""\negate -4""", true) shouldBe "4"
    a[RuntimeException] should be thrownBy { test("""\negate asdf""", false) }
  }

  "nil" in {
    test("""\nil{}""", false) shouldBe ""
  }

  "normalize" in {
    test("""\normalize { this   is a     boring  test }""", false) shouldBe "this is a boring test"
  }

  "now" in {
    test("""\now | date "MMMM d, y"""", false) shouldBe today
  }

  "range" in {
    test("""\range 3 6""", true) shouldBe "[3, 4, 5, 6]"
    test("""\range 3.1 6""", true) shouldBe "[3.1, 4.1, 5.1]"
    test("""\range 3 2""", true) shouldBe "[]"
    a[RuntimeException] should be thrownBy { test("""\range 3 asdf""", false) }
    a[RuntimeException] should be thrownBy { test("""\range asdf 3""", false) }
  }

  "rem" in {
    test("""\rem 8 3""", true) shouldBe "2"
    a[RuntimeException] should be thrownBy { test("""\rem asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\rem 1 asdf""", false) }
  }

  "remove" in {
    test("""\remove "rain" "I strained to see the train through the rain"""",
         false) shouldBe """I sted to see the t through the """
    a[RuntimeException] should be thrownBy { test("""\remove asdf 1""", false) }
    a[RuntimeException] should be thrownBy { test("""\remove 1 asdf""", false) }
  }

  "removeFirst" in {
    test(
      """\removeFirst "rain" "I strained to see the train through the rain"""",
      false) shouldBe """I sted to see the train through the rain"""
    a[RuntimeException] should be thrownBy {
      test("""\removeFirst asdf 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\removeFirst 1 asdf""", false)
    }
  }

  "replace" in {
    test(
      """\replace "my" "your" "Take my protein pills and put my helmet on"""",
      false) shouldBe """Take your protein pills and put your helmet on"""
    a[RuntimeException] should be thrownBy {
      test("""\replace asdf 1 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\replace 1 asdf 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\replace 1 1 asdf""", false)
    }
  }

  "replaceFirst" in {
    test(
      """\replaceFirst "my" "your" "Take my protein pills and put my helmet on"""",
      false) shouldBe """Take your protein pills and put my helmet on"""
    a[RuntimeException] should be thrownBy {
      test("""\replaceFirst asdf 1 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\replaceFirst 1 asdf 1""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\replaceFirst 1 1 asdf""", false)
    }
  }

  "reverse" in {
    test("""\seq {3 4 5} | reverse""", true) shouldBe "[5, 4, 3]"
    test("""\reverse asdf""", true) shouldBe "fdsa"
    a[RuntimeException] should be thrownBy { test("""\reverse 123""", false) }
  }

  "round" in {
    test("""\round 5.2 \round 5.6 \round 1.23 scale: 1 \round 1.26 scale: 1""",
         true) shouldBe "5 6 1.2 1.3"
    a[RuntimeException] should be thrownBy { test("""\round asdf""", false) }
  }

  "size" in {
    test("""\seq {3 4 5} | size""", true) shouldBe "3"
    test("""\{a 3 b 4} | size""", true) shouldBe "2"
    test("""\size asdf""", true) shouldBe "4"
    a[RuntimeException] should be thrownBy { test("""\size 123""", false) }
  }

  "slice" in {
    test("""\seq {3 4 5 6 7} | slice 2 4""", true) shouldBe "[5, 6]"
    test("""\slice 2 4 asdf""", true) shouldBe "df"
    a[RuntimeException] should be thrownBy {
      test("""\slice 123 123 123""", false)
    }
  }

  "sort" in {
    test("""\seq {\timestamp 5 \timestamp 4 \timestamp 3} | sort""", true) shouldBe "[1970-01-01T00:00:00.003Z, 1970-01-01T00:00:00.004Z, 1970-01-01T00:00:00.005Z]"
    test("""\seq {3 5 4 7 6 2 1} | sort""", true) shouldBe "[1, 2, 3, 4, 5, 6, 7]"
    test(
      """\seq {\{a 3} \{a 5} \{a 4} \{a 7} \{a 6} \{a 2} \{a 1}} | sort on: a order: desc""",
      true) shouldBe """[{"a": 7}, {"a": 6}, {"a": 5}, {"a": 4}, {"a": 3}, {"a": 2}, {"a": 1}]"""
    test("""\seq {\{a 3} \{a 5} \{a 4}} | sort""", true) shouldBe """[{"a": 3}, {"a": 4}, {"a": 5}]"""
    a[RuntimeException] should be thrownBy { test("""\sort 123""", false) }
  }

  "split" in {
    test("""\split "\\s+" "This is a sentence"""", true) shouldBe """["This", "is", "a", "sentence"]"""
    a[RuntimeException] should be thrownBy { test("""\split 123 123""", false) }
  }

  "tail" in {
    test("""\seq {3 4 5} | tail""", true) shouldBe "[4, 5]"
    test("""\tail asdf""", true) shouldBe "sdf"
    a[RuntimeException] should be thrownBy { test("""\tail 123""", false) }
    a[RuntimeException] should be thrownBy { test("""\tail \[]""", false) }
  }

  "take" in {
    test("""\take 2 \seq {3 4 5 6 7}""", true) shouldBe "[3, 4]"
    test("""\take 2 asdf""", true) shouldBe "as"
    a[RuntimeException] should be thrownBy { test("""\take asdf 123""", false) }
    a[RuntimeException] should be thrownBy { test("""\take 123 123""", false) }
  }

  "timestamp" in {
    test("""\timestamp "2018-06-05T14:52:25Z" | date "MMMM d, y"""", true) shouldBe """June 5, 2018"""
    test("""\timestamp 1528314399243 | date "MMMM d, y"""", true) shouldBe """June 6, 2018"""
    a[RuntimeException] should be thrownBy { test("""\timestamp \[]""", false) }
  }

  "toInteger" in {
    test("""\+ 0 \toInteger -3""", true) shouldBe "-3"
    test("""\toInteger 3.1""", true) shouldBe "3"
    test("""\toInteger -3.1""", true) shouldBe "-3"
    a[RuntimeException] should be thrownBy {
      test("""\toInteger asdf""", false)
    }
  }

  "toNumber" in {
    test("""\set int "3."\set frac "5"\+ 1 \toNumber \+ \int \frac""", false) shouldBe "4.5"
    a[RuntimeException] should be thrownBy {
      test("""\toNumber "asdf"""", false)
    }
  }

  "toString" in {
    test("""\{a 3 b 4} | toString""", true) shouldBe """{"a": 3, "b": 4}"""
  }

  "today" in {
    test("""\today""", false) shouldBe today
  }

  "trim" in {
    test(""">>\trim {  Hello World!   }<<""", true) shouldBe ">>Hello World!<<"
    a[RuntimeException] should be thrownBy { test("""\trim 123""", false) }
  }

  "u" in {
    test("\\u 0x61", true) shouldBe "a"
    a[RuntimeException] should be thrownBy { test("\\u asdf", false) }
    a[RuntimeException] should be thrownBy { test("\\u 123456789", false) }
  }

  "upcase" in {
    test("\\upcase {Hello World!}", true) shouldBe "HELLO WORLD!"
    a[RuntimeException] should be thrownBy { test("\\upcase 123", false) }
  }

  "empty-object" in {
    test("""\{}""", true) shouldBe "{}"
  }

}
