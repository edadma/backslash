package xyz.hyperreal.backslash

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LanguageTests extends AnyFreeSpec with Matchers with Testing {

  val today = ZonedDateTime.now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

  "basic" in {
    test("", false) shouldBe ""
    test("Hello World!", false) shouldBe "Hello World!"
    test("""Today is \today .""", false) shouldBe s"Today is $today."
    test("""Today is \today.""", false) shouldBe s"Today is $today."
    test("""3 plus 4 is \+ 3 4 .""", false) shouldBe "3 plus 4 is 7 ."
    test("""3 plus 4 is \+ 3 4{}.""", false) shouldBe "3 plus 4 is 7."
    test("""3 plus 4 is {\+ 3 4}.""", false) shouldBe "3 plus 4 is 7."
  }

  "def" in {
    test("""
        |\def fac n {\if \=\n0 1 \else \*\n\fac\-\n1}
        |
        |\fac 5
      """.stripMargin,
         true) shouldBe
      """
        |120
      """.trim.stripMargin
    test("""
        |\def m {asdf}
        |\m, \m
      """.stripMargin,
         true) shouldBe
      """
        |asdf, asdf
      """.trim.stripMargin
    test("""
        |\def m a {as\a df}
        |\m { blah blah }, \m ***
      """.stripMargin,
         true) shouldBe
      """
        |as blah blah df, as***df
      """.trim.stripMargin
    a[RuntimeException] should be thrownBy { test("""\def m asdf""", false) }
    a[RuntimeException] should be thrownBy { test("""\def m {asdf""", false) }
    a[RuntimeException] should be thrownBy { test("""\def {asdf}""", false) }
  }

  "dot" in {
    test("""\. \{a 3} a""", false) shouldBe "3"
    test("""\set v \{a \{b 3}}\v.a.b""", false) shouldBe "3"
    test("""\set v \{a \{b \{c 3}}}\v.a.b.c""", false) shouldBe "3"
    test("""\. "asdf" 2""", false) shouldBe "d"
    test("""\. \seq {3 4 5 6} 2""", false) shouldBe "5"
    test("""\= \nil{} \. \{a 3} b""", false) shouldBe "true"
    test("""\set v \{a 3} v.a=\v.a\ and v=\v.""", false) shouldBe """ v.a=3 and v={"a": 3}."""
    a[RuntimeException] should be thrownBy { test("""\. 123 123""", false) }
  }

  "seq" in {
    test("""\seq {1 2 3}""", false) shouldBe "[1, 2, 3]"
    test("""\seq {1 2 3} | map \+ _ 3""", false) shouldBe "[4, 5, 6]"
    test("""\set v 4\seq {1 2 3 \v}""", false) shouldBe "[1, 2, 3, 4]"
    test("""\seq {}""", false) shouldBe "[]"
    test("""\[]""", false) shouldBe "[]"
    a[RuntimeException] should be thrownBy { test("""\seq {3 4 5""", false) }
    a[RuntimeException] should be thrownBy { test("""\seq 123""", false) }
  }

  "obj" in {
    test("""\{three 3 four 4 five 5}""", false) shouldBe """{"three": 3, "four": 4, "five": 5}"""
    test("""\set v 6\{three 3 four 4 five 5 six \v}""", false) shouldBe """{"three": 3, "four": 4, "five": 5, "six": 6}"""
    test("""\{}""", false) shouldBe """{}"""
    a[RuntimeException] should be thrownBy { test("""\{a 3 b 4 c 5""", false) }
  }

  "literals" in {
    test("""\+ 0x12 1""", false) shouldBe "19"
    test("\\set v '\\u0061'\\v", false) shouldBe "a"
//    test( """asdf \n\t zxvc""", false ) shouldBe "asdf \n\tzxvc"
    test("""asdf \set v '"\b\f\n\r\t\\\'\"'\v zxvc""", false) shouldBe "asdf \"\b\f\n\r\t\\\'\"zxvc"
    test("""asdf \set v "'\b\f\n\r\t\\\'\""\v zxvc""", false) shouldBe "asdf '\b\f\n\r\t\\\'\"zxvc"
    test("""asdf \set v 123.4\+ \v 1 zxvc""", false) shouldBe "asdf 124.4 zxvc"
    test("""asdf \set v -3\+ \v 1 zxvc""", false) shouldBe "asdf -2 zxvc"
    test("""asdf \set v -3.4\+ \v 1 zxvc""", false) shouldBe "asdf -2.4 zxvc"
    test("""\seq {true false null}""", false) shouldBe "[<true>, <false>, <null>]"
    a[RuntimeException] should be thrownBy {
      test("""\split "a b" null""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\split "a b" true""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""\split "a b" false""", false)
    }
    a[RuntimeException] should be thrownBy {
      test("""asdf \set v "'\b\f\n\r\t\\\'\"""", false)
    }
  }

  "if" in {
    test("""start \if v defined end""", true) shouldBe "start end"
    test("""start \if v defined end""", true, "v" -> 123) shouldBe "start defined end"
    test("""start \if v defined \elsif x x end""", true) shouldBe "start end"
    test("""start \if v defined \elsif x x end""", true, "x" -> 123) shouldBe "start x end"
    test("""start \if v defined \elsif x x end""", true, "v" -> 123) shouldBe "start defined end"
    test("""start \if v defined \elsif x x \else undefined end""", true) shouldBe "start undefined end"
    test("""start \if v defined \elsif x x \else undefined end""", true, "x" -> 123) shouldBe "start x end"
    test("""start \if v defined \elsif x x \else undefined end""", true, "v" -> 123) shouldBe "start defined end"
    test("""start \if v defined \else undefined end""", true) shouldBe "start undefined end"
    test("""start \if v defined \else undefined end""", true, "v" -> 123) shouldBe "start defined end"
  }

  "unless" in {
    test("start \\unless v undefined end", true) shouldBe "start undefined end"
    test("start \\unless v undefined end", true, "v" -> 123) shouldBe "start end"
    test("start \\unless v undefined \\else defined end", true) shouldBe "start undefined end"
    test("start \\unless v undefined \\else defined end", true, "v" -> 123) shouldBe "start defined end"
  }

  "in" in {
    a[RuntimeException] should be thrownBy {
      test("""\in e l""", true, "l" -> List("a", "b", "c"))
    }
    a[RuntimeException] should be thrownBy { test("""\in e l""", true) }
  }

  "for" in {
    test("""start \for l \forloop.element end""", true, "l" -> List("a", "b", "c")) shouldBe
      "start abcend"
    test("""start \for \in e l \e end""", true, "l" -> List("a", "b", "c")) shouldBe
      "start abcend"
    test("""start \for l {\forloop.element} end""", true, "l" -> List("a", "b", "c")) shouldBe
      "start abc end"
    test("""start \for l {\forloop.element\ } end""", true, "l" -> List("a", "b", "c")) shouldBe
      "start a b c end"
    test("""start \for l {\forloop.element\ } \else else end""", true, "l" -> List("a", "b", "c")) shouldBe
      "start a b c else end"
    test("""start \for m \f end""", true, "m" -> Map("f" -> 123)) shouldBe
      "start 123end"
  }

  "filters" in {
    test("""\lit {this is a test} | replace 'is' '**' | remove test | size""", true) shouldBe
      "10"
    test("""\l | \reverse | \take 2 | \drop 1""", true, "l" -> List("a", "b", "c")) shouldBe
      """["b"]"""
    test("""\l | map \+ _ 1 | filter \> _ 5""", true, "l" -> List[BigDecimal](3, 4, 5, 6, 7)) shouldBe
      """[6, 7, 8]"""
    test("""\seq {\{a 3} \{b 4} \{a 5}} | map a | filter _""", false) shouldBe "[3, 5]"
    test("""\def m {asdf}\m | upcase""", false) shouldBe "ASDF"
    a[RuntimeException] should be thrownBy { test("""\seq {1} | \n""", true) }
    a[RuntimeException] should be thrownBy {
      test("""\seq {1} | \asdf""", true)
    }
    a[RuntimeException] should be thrownBy {
      test("""\def m {asdf} \seq {1} | \m""", true)
    }
  }

  "break" in {
    test("""start \for l {\if \> \forloop.indexz 1 \break \forloop.element\ } \else else end""",
         true,
         "l" -> List("a", "b", "c")) shouldBe
      "start a b end"
    test("""start \for l {\if \> \forloop.indexz 1 \break {\forloop.element\ }} end""",
         true,
         "l" -> List("a", "b", "c")) shouldBe
      "start a b end"
    a[RuntimeException] should be thrownBy { test("""\break""", true) }
  }

  "continue" in {
    test("""start \for l {\if \= \forloop.indexz 1 \continue \forloop.element\ } \else else end""",
         true,
         "l" -> List("a", "b", "c")) shouldBe
      "start a c else end"
    test("""start \for l {\if \= \forloop.indexz 1 \continue {\forloop.element\ }} end""",
         true,
         "l" -> List("a", "b", "c")) shouldBe
      "start a c end"
    a[RuntimeException] should be thrownBy { test("""\continue""", true) }
  }

  "connectives" in {
    test("""start {\and true true} end""", true) shouldBe
      "start true end"
    test("""start {\and true false} end""", true) shouldBe
      "start false end"
    test("""start {\and true v} end""", true, "v" -> 123) shouldBe
      "start true end"
    test("""start {\and true v} end""", true) shouldBe
      "start false end"
    test("""start {\or true true} end""", true) shouldBe
      "start true end"
    test("""start {\or true false} end""", true) shouldBe
      "start true end"
    test("""start {\or v false} end""", true) shouldBe
      "start false end"
    test("""start {\or v false} end""", true, "v" -> 123) shouldBe
      "start 123 end"
    test("""start {\or false v} end""", true, "v" -> 123) shouldBe
      "start 123 end"
    test("""start {\not true} end""", true) shouldBe
      "start false end"
    test("""start {\not v} end""", true) shouldBe
      "start true end"
    test("""start {\not v} end""", true, "v" -> 123) shouldBe
      "start false end"
  }

  "set" in {
    test("""\set v {have a nice day}\v""", true) shouldBe
      "have a nice day"
    test("""\set v \+ 3 4\v""", true) shouldBe
      "7"
    a[RuntimeException] should be thrownBy { test("""\set set asdf""", true) }
    a[RuntimeException] should be thrownBy { test("""\set map asdf""", true) }
  }

  "match" in {
    test("""start \match 5 end""", true) shouldBe
      "start end"
    test("""start \match 5 \case 3 three end""", true) shouldBe
      "start end"
    test("""start \match 5 \case 3 three \else else end""", true) shouldBe
      "start else end"
    test("""start \match 5 \case 5 five end""", true) shouldBe
      "start five end"
    test("""start \match 5 \case 3 three \case 5 five end""", true) shouldBe
      "start five end"
    test("""start \match 5 \case 5 five \case 3 three end""", true) shouldBe
      "start five end"
    test("""start \match 5 \case 5 five \else else end""", true) shouldBe
      "start five end"
  }

  "comments" in {
    test("""3 plus \#super boring\#4 is {\+ 3 4}.""", false) shouldBe "3 plus 4 is 7."
    test("""3 plus\# super boring \# 4 is {\+ 3 4}.""", false) shouldBe "3 plus 4 is 7."
    test(
      """
        |\#
        |  This is the start of a very boring comment.
        |
        |  This is the end of a very boring comment.
        |\#
        |
        |Today is \today .
      """.stripMargin,
      true
    ) shouldBe s"Today is $today."
    a[RuntimeException] should be thrownBy {
      test("""3 plus \#super boring""", false)
    }
  }

  "raw" in {
    test("""
        |\<<<
        |\set v 'asdf'
        |\v
        |\>>> wow
      """.stripMargin,
         false) shouldBe
      """
        |\set v 'asdf'
        |\v
        |wow
      """.stripMargin
    test("""
        |\raw {{ }}
        |\{{
        |\set v 'asdf'
        |\v
        |\}} wow
      """.stripMargin,
         false) shouldBe
      """
        |
        |\set v 'asdf'
        |\v
        |wow
      """.stripMargin
  }

  "delim" in {
    test("""
        |\delim \\ {{ }}
        |Today is \today .
      """.stripMargin,
         true) shouldBe s"Today is \\today ."
    test("""
        |\delim \\ {{ }}
        |3 plus 4 is \\+ 3 4 .
      """.stripMargin,
         true) shouldBe "3 plus 4 is 7 ."
    test("""
        |\delim \\ {{ }}
        |3 plus 4 is \\+ 3 4{{}}.
      """.stripMargin,
         true) shouldBe "3 plus 4 is 7."
    test("""
        |\delim \\ {{ }}
        |3 plus 4 is {{\\+ 3 4}}.
      """.stripMargin,
         true) shouldBe "3 plus 4 is 7."
  }

}
