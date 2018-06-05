---
title: Regular Command Reference
---

Regular Commands
================

This page details all the built-in regular commands in lexicographical order.

Summary
-------

<nav>
  <ul>
    <li><a href="#">*</a></li>
    <li><a href="#-1">+</a></li>
    <li><a href="#-">-</a></li>
    <li><a href="#-2">..</a></li>
  </ul>
</nav>


*
-

### Description

Multiplies two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the product as a numerical result.

### Example

    \* 3 4

output

    12

### Exceptions

If the arguments are not numerical, an exception is thrown.


+
-

### Description

Adds two numbers, or concatenates two sequences or two strings or two objects, or appends or prepends an item to a sequence.

### Input

Takes two numerical arguments, or two sequences, or two objects, or two strings, or a non-sequence item and a sequence.

### Output

Returns the sum as a numerical result, or concatenation of two sequences or an item and a sequence as a sequence, or concatenation of two strings as a string, or concatenation of two objects as an object.

### Example

    \+ 3 4

output

    7

### Exceptions

If the arguments do not have the combination of types listed under "Input", an exception is thrown.


-
-

### Description

Computes the difference of two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the difference as a numerical result.

### Example

    \- 3 4

output

    -1

### Exceptions

If the arguments are not numerical, an exception is thrown.


..
--

### Description

Creates a sequence ranging from one number to the other.  This command does not actually enumerate over all the numbers in the range to create the sequence, it creates an object that behaves as if it were that sequence (i.e. runs in O(1) time).

### Input

Takes two numerical arguments.

### Output

Returns the range as a sequence result.

### Example

    \.. 3 6

output

    [3, 4, 5, 6]

### Exceptions

If the arguments are not numerical, an exception is thrown.


/
-

### Description

Computes the quotient of two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the quotient as a numerical result.

### Example

    \/ 3 4

output

    0.75

### Exceptions

If the arguments are not numerical, an exception is thrown.


/=
--

### Description

Tests for inequality.

### Input

Takes two arguments of any kind.

### Output

Returns `true` if the arguments are unequal, `false` otherwise.

### Example

    \/= 3 4

output

    true

### Exceptions

none


<
-

### Description

Tests whether one argument is less than the other.

### Input

Takes either two numerical arguments or two string arguments.

### Output

Returns `true` if the first argument is less than the other, `false` otherwise.  For numerical arguments, "less than" has the usual mathematical meaning.  For string arguments, the comparison is done lexicographically, with the shorter string considered to be less than the longer if they are of unequal length and the shorter is equal to a prefix of the longer.

### Example

    \< 3 4

output

    true

### Exceptions

If the arguments are not both numerical or both strings, an exception is thrown.


<=
--

### Description

Tests whether one argument is less than or equal to the other.

### Input

Takes either two numerical arguments or two string arguments.

### Output

Returns `true` if the first argument is less than or equal to the other, `false` otherwise.  For numerical arguments, "less than or equal" has the usual mathematical meaning.  For string arguments, the comparison is done lexicographically, with the shorter string considered to be less than the longer if they are of unequal length and the shorter is equal to a prefix of the longer.

### Example

    \<= 3 4

output

    true

### Exceptions

If the arguments are not both numerical or both strings, an exception is thrown.


=
-

### Description

Tests for equality.

### Input

Takes two arguments of any kind.

### Output

Returns `true` if the arguments are equal, `false` otherwise.

### Example

    \= 3 4

output

    false

### Exceptions

none


&gt;
-

### Description

Tests whether one argument is greater than the other.

### Input

Takes either two numerical arguments or two string arguments.

### Output

Returns `true` if the first argument is greater than the other, `false` otherwise.  For numerical arguments, "greater than" has the usual mathematical meaning.  For string arguments, the comparison is done lexicographically, with the shorter string considered to be greater than the longer if they are of unequal length and the shorter is equal to a prefix of the longer.

### Example

    \> 3 4

output

    false

### Exceptions

If the arguments are not both numerical or both strings, an exception is thrown.


&gt;=
--

### Description

Tests whether one argument is greater than or equal to the other.

### Input

Takes either two numerical arguments or two string arguments.

### Output

Returns `true` if the first argument is greater than or equal to the other, `false` otherwise.  For numerical arguments, "greater than or equal" has the usual mathematical meaning.  For string arguments, the comparison is done lexicographically, with the shorter string considered to be greater than the longer if they are of unequal length and the shorter is equal to a prefix of the longer.

### Example

    \>= 3 4

output

    false

### Exceptions

If the arguments are not both numerical or both strings, an exception is thrown.


[]
--

### Description

The empty sequence.

### Input

none

### Output

Returns the empty sequence.

### Example

    \[]

output

    []

### Exceptions

none


^
-

### Description

Raises one number to the power of the other.

### Input

Takes two numerical arguments: the first can be any number, the second be an integer in the range -2147483648 to 2147483647.

### Output

Returns the first argument raised to the power of the second.

### Example

    \^ 3 4

output

    81

### Exceptions

If the arguments are not numerical, an exception is thrown.


abs
---

### Description

Absolute value function.

### Input

Takes one numerical argument.

### Output

Returns the absolute value of the argument.

### Example

    \abs -5

output

    5

### Exceptions

If the argument is not numerical, an exception is thrown.


append
------

### Description

Appends one value to another.

### Input

Takes either two string arguments, or an argument of any kind and a sequence.

### Output

Returns the first argument append to the second.  If the arguments are strings, then the first is appended to the end of the second producing a new string.  Otherwise, the first argument is appended to the end of the sequence producing a new sequence.

### Example

    \append ample ex

output

    example

### Exceptions

If the arguments are not strings or the second is not a sequence, an exception is thrown.


ceil
----

### Description

Ceiling function.  Maps the argument to the least integer greater than or equal to it.

### Input

Takes one numerical argument.

### Output

Returns the ceiling of the argument.

### Example

    \ceil -5.1

output

    -5

### Exceptions

If the argument is not numerical, an exception is thrown.


contains
--------

### Description

Tests whether one value is contained within another.

### Input

Takes either two string arguments, or a sequence or object and an argument of any kind.

### Output

Returns `true` if the first argument is contained within the second, `false` otherwise.

### Example

    \contains {the truth is out there} truth

output

    true

### Exceptions

If the arguments are not strings or the first is not a sequence or an object, an exception is thrown.


date
----

### Description

Formats a date.

### Input

Takes a string and a date (i.e. [TemporalAccessor](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalAccessor.html)).

### Output

Returns the second argument formatted according to the pattern given by the first (see [Date Format Patterns](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)).

### Example

    \now | date "MMMM d, y"

output

    June 1, 2018

### Exceptions

If the arguments are not strings or the first is not a sequence or an object, an exception is thrown.


default
-------

### Description

Provides a default as a fallback in case a value doesn't exit.

### Input

Takes two arguments of any kind.

### Output

Returns the second argument (unchanged), unless it is `nil` in which case the "default" first value is returned.

### Example

    \price | default 2.99

output (assuming the variable `price` is *not* defined)

    2.99

### Exceptions

none


distinct
--------

### Description

Removes duplicates from a sequence.

### Input

Takes one sequence argument.

### Output

Returns a new sequence containing only distinct values.

### Example

    \seq {1 2 3 2 4 3 5} | distinct

output

    [1, 2, 3, 4, 5]

### Exceptions

If the argument is not a sequence, an exception is thrown.


downcase
--------

### Description

Makes all characters in a string lower case.

### Input

Takes a string argument.

### Output

Returns a new string containing only lower case characters.

### Example

    \downcase {Hello World!}

output

    hello world!

### Exceptions

If the argument is not a string, an exception is thrown.


drop
----

### Description

Drops items from the head of a sequence or string.

### Input

Takes an integer argument and a string or sequence argument.

### Output

If the second argument is a string, returns a new string skipping over the number of characters given by the first argument.  If the second argument is a sequence, returns a new sequence skipping over the number of items given by the first argument.

### Example

    \drop 2 \seq {3 4 5 6 7}

output

    [5, 6, 7]

### Exceptions

If the first argument is not a number and the second is not a string or a sequence, an exception is thrown.


escape
------

### Description

Replaces characters in a string with HTML entity equivalents.

### Input

Takes one string argument.

### Output

Returns a new string with certain characters changed to their HTML entity equivalents.

### Example

    \escape {a < b}

output

    a &lt; b

### Exceptions

If the argument is not a string, an exception is thrown.


escapeOnce
----------

### Description

Replaces characters in a string with HTML entity equivalents while avoiding any HTML entities that may already be present.

### Input

Takes one string argument.

### Output

Returns a new string with certain characters changed to their HTML entity equivalents leaving existing HTML entities unchanged.

### Example

    \escapeOnce {a < b &lt; c}

output

    a &lt; b &lt; c

### Exceptions

If the argument is not a string, an exception is thrown.


false
-----

### Description

False.

### Input

none

### Output

Returns `false`.

### Example

    \false

output

    false

### Exceptions

none


filter
------

### Description

Selects all elements of the sequence that satisfy the lambda expression.

### Input

Takes a lambda expression and a sequence as arguments.

### Output

Returns a new sequence with elements that satisfy the predicate.

### Example

    \seq {1 2 3 4} | filter \> _ 2

output

    [3, 4]

### Exceptions

If the second argument is not a sequence, an exception is thrown.


floor
-----

### Description

Floor function.  Maps the argument to the greatest integer less than or equal to it.

### Input

Takes one numerical argument.

### Output

Returns the floor of the argument.

### Example

    \floor -5.1

output

    -6

### Exceptions

If the argument is not numerical, an exception is thrown.


head
----

### Description

Selects the first element of the sequence or string.

### Input

Takes a sequence or string argument.

### Output

Returns the first element of the sequence or string.

### Example

    \seq {3 4 5} | head

output

    3

### Exceptions

If the argument is not a sequence or string, or the sequence is empty, an exception is thrown.


include
-------

### Description

Reads a file as a Backslash template.

### Input

Takes a string argument as the path to the file to be included.  The path is relative to the `include` configuration property.

### Output

Returns the rendered Backslash file.

### Example

    \include "sections/header.bac"

output



### Exceptions

An exception may be thrown during the rendering of the included file.


integer
-------

### Description

Conversion to integer.

### Input

Takes a numerical or string argument.

### Output

If the argument is a string, parses it as a number and converts it to an integer.  If the argument is a number, converts it to an integer.

### Example

    \integer -5.1

output

    -5

### Exceptions

If the argument is not a string or numerical, an exception is thrown.


join
----

### Description

Concatenates all elements of a sequence into a string.

### Input

Takes a string and a sequence.

### Output

Returns the concatenation of all elements of a sequence into a string, placing the given separator between each element.

### Example

    \join " - " \seq {1 2 3}

output

    1 - 2 - 3

### Exceptions

If the first argument is not a string and the second is not a sequence, an exception is thrown.


last
----

### Description

Selects the last element of a sequence or string.

### Input

Takes a sequence or string argument.

### Output

Returns the last element of the sequence or string.

### Example

    \seq {3 4 5} | last

output

    5

### Exceptions

If the argument is not a sequence or string, or the sequence is empty, an exception is thrown.


lit
---

### Description

The identity function (`lit` is short for "literal").

### Input

One argument of any kind.

### Output

Returns the argument.

### Example

    \lit {this is a test} | replace 'is' '**'

output

    th** ** a test

### Exceptions

none


map
---

Transforms a sequence by applying a lambda expression to all elements of the sequence.

### Input

Takes a lambda expression and a sequence as arguments.

### Output

Returns a new sequence with all elements transformed by the lambda expression.  If the lambda expression is a string, then it is treated as a short hand for `\. _ <string argument>`.

### Example

    \seq {3 4 5} | map \+ _ 2

output

    [5, 6, 7]

### Exceptions

If the second argument is not a sequence, an exception is thrown.


markdown
--------

### Description

Processes a string as a Markdown, transforming it into HTML.

### Input

Takes a string argument.

### Output

Returns the HTML corresponding to the Markdown text.

### Example

    \markdown {this is a __boring__ *test*}

output

    <p>this is a <strong>boring</strong> <em>test</em></p>

### Exceptions

none


max
---

### Description

Returns the greater of two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the greater of the two arguments.

### Example

    \max 3 4

output

    4

### Exceptions

If the arguments are not numerical, an exception is thrown.


min
---

### Description

Returns the lesser of two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the lesser of the two arguments.

### Example

    \min 3 4

output

    3

### Exceptions

If the arguments are not numerical, an exception is thrown.


n
-

### Description

The newline character (\u000A).

### Input

none

### Output

Returns a string containing only the newline character.

### Example

    \n

output



### Exceptions

none


negate
------

### Description

Negation.

### Input

Takes one numerical argument.

### Output

Returns the negative of the argument.

### Example

    \negate -5

output

    5

### Exceptions

If the argument is not numerical, an exception is thrown.


nil
---

### Description

Evaluates it's argument discarding the result and returning a `nil`.

### Input

One argument of any kind.

### Output

Returns a `nil` after evaluating the argument.

### Example

    \nil {\do_something}

output

    `nil`

### Exceptions

none


normalize
---------

### Description

Normalizes a string by removing whitespace from both ends of the string and converting any stretches of whitespace within the string to a single space character.

### Input

Takes a string argument.

### Output

Returns the normalized argument.

### Example

    \normalize { this   is a     boring  test }

output

    this is a boring test

### Exceptions

none


now
---

### Description

The current time as a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html).

### Input

none

### Output

Returns the current time.

### Example

    \now

output

    2018-06-03T11:32:50.875-04:00[America/Montreal]

### Exceptions

none


null
----

### Description

The `null`.

### Input

none

### Output

Returns a `null`.

### Example

    \null

output

    `null`

### Exceptions

none


number
------

### Description

Conversion to a number.

### Input

Takes a numerical or string argument.

### Output

If the argument is a string, parses it as a number and returns a numerical result.  If the argument is a number, it is returned as is.

### Example

    \+ \number "123" 1

output

    124

### Exceptions

If the argument is not a string or numerical, an exception is thrown.


rem
---

### Description

Computes the remainder of two numbers.

### Input

Takes two numerical arguments.

### Output

Returns the remainder as a numerical result.

### Example

    \rem 8 3

output

    2

### Exceptions

If the arguments are not numerical, an exception is thrown.


remove
------

### Description

Removes every occurrence of a substring from a string.

### Input

Takes two string arguments.

### Output

Returns the second string with every occurrence of the first removed.

### Example

    \remove "rain" "I strained to see the train through the rain"

output

    I sted to see the t through the

### Exceptions

If the arguments are not strings, an exception is thrown.


removeFirst
-----------

### Description

Removes the first occurrence of a substring from a string.

### Input

Takes two string arguments.

### Output

Returns the second string with the first occurrence of the first removed.

### Example

    \removeFirst "rain" "I strained to see the train through the rain"

output

    I sted to see the train through the rain

### Exceptions

If the arguments are not strings, an exception is thrown.


replace
-------

### Description

Replaces every occurrence of a substring from a string.

### Input

Takes three string arguments.

### Output

Returns the third argument with every occurrence of the first replaced with the second.

### Example

    \replace "my" "your" "Take my protein pills and put my helmet on"

output

    Take your protein pills and put your helmet on

### Exceptions

If the arguments are not strings, an exception is thrown.


replaceFirst
------------

### Description

Replaces the first occurrence of a substring from a string.

### Input

Takes three string arguments.

### Output

Returns the third argument with the first occurrence of the first replaced with the second.

### Example

    \replaceFirst "my" "your" "Take my protein pills and put my helmet on"

output

    Take your protein pills and put my helmet on

### Exceptions

If the arguments are not strings, an exception is thrown.


reverse
-------

### Description

Reverses a sequence or string.

### Input

Takes a sequence or string argument.

### Output

Returns the sequence or string with all elements in reverse order.

### Example

    \seq {3 4 5} | reverse

output

    [5, 4, 3]

### Exceptions

If the argument is not a sequence or string, an exception is thrown.


round
-----

### Description

Rounds a number to a given scale (0 by default) according to the rounding mode given by the *rounding* configuration property (`HALF_EVEN` or "banker's rounding" is the default).

### Input

Takes one numerical argument, as well as an optional `scale` argument.

### Output

Returns the rounded value of the argument.

### Example

    \round 5.2 \round 5.6 \round 1.23 scale: 1 \round 1.26 scale: 1

output

    5 6 1.2 1.3

### Exceptions

If the argument is not numerical, an exception is thrown.


size
----

### Description

The size of a sequence, object or string.

### Input

Takes a sequence, object or string argument.

### Output

Returns the number of elements in the sequence, or the number of properties in the object, or the number of characters in the string.

### Example

    \seq {3 4 5} | size

output

    3

### Exceptions

If the argument is not a sequence or string, an exception is thrown.


slice
-----

### Description

A slice from a sequence or string.

### Input

Takes a numerical argument giving the start index of the slice, a numerical argument giving the end index of the slice (exclusive), and a sequence or string argument.

### Output

Returns a sub-sequence from a sequence or substring from a string.

### Example

    \seq {3 4 5 6 7} | slice 2 4

output

    [5, 6]

### Exceptions

If the argument is not a sequence or string, an exception is thrown.


sort
----

### Description

A sorted sequence.

### Input

Takes a sequence argument, an optional `on` argument specifying which property to sort on, and an optional `order` argument specifying the sort order.  The `order` argument can be either `asc` (the default) or `desc`.

### Output

Returns a sorted sequence.

### Example

    \seq {3 5 4 7 6 2 1} | sort

output

    [1, 2, 3, 4, 5, 6, 7]

### Exceptions

If the argument is not a sequence, an exception is thrown.


split
-----

### Description

Splits a string into a sequence of substrings around a separator.

### Input

Takes two string arguments: the separator, and the string to be split.

### Output

Returns the second string split into a sequence using the first as a separator.

### Example

    \split "\\s+" "This is a sentence"

output

    ["This", "is", "a", "sentence"]

### Exceptions

If the arguments are not strings, an exception is thrown.


string
------

### Description

Converts to a *display* string.  A *display* string is a readable string representation of something that's not a string.

### Input

Takes one argument of any kind.

### Output

Returns a new string displaying the contents of the argument in a readable manner.  argument is already a string, it is left unchanged.  Numbers appear as expected.

### Example

    \{a 3 b 4} | string

output

    {"a": 3, "b": 4}

### Exceptions

none


t
-

### Description

The tab character (\u0009).

### Input

none

### Output

Returns a string containing only the tab character.

### Example

    \t

output



### Exceptions

none


tail
----

### Description

Selects all elements other than the first of a sequence or string.

### Input

Takes a sequence or string argument.

### Output

Returns every element after the first element of the sequence or string.

### Example

    \seq {3 4 5} | tail

output

    [4, 5]

### Exceptions

If the argument is not a sequence or string, or the sequence is empty, an exception is thrown.


take
----

### Description

Takes a specified number of items from the head of a sequence or string.

### Input

Takes an integer argument and a string or sequence argument.

### Output

If the second argument is a string, returns a new string containing only the number of characters given by the first argument.  If the second argument is a sequence, returns a new sequence containing only the number of items given by the first argument.

### Example

    \take 2 \seq {3 4 5 6 7}

output

    [3, 4]

### Exceptions

If the first argument is not a number and the second is not a string or a sequence, an exception is thrown.


timestamp
---------

### Description

Converts a string containing a valid .

### Input

Takes a string argument.

### Output

If the argument is a string, returns a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) object corresponding to the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) timestamp (date and time representation) input string.  If the argument is an integer, returns an [Instant](https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html) corresponding to the Unix epoch time in milliseconds given by the integer argument.

### Example

    \timestamp "2018-06-05T14:52:25Z" | date "MMMM d, y"

output

    June 5, 2018

### Exceptions

If the argument is not a string or an integer, an exception is thrown.


today
-----

### Description

The current date.

### Input

none

### Output

Returns a string representing the current date formatted according to the `today` configuration property.

### Example

    \today

output

    June 5, 2018

### Exceptions

none


trim
----

### Description

Removes all whitespace characters from both ends of a string.

### Input

Takes a string argument.

### Output

Returns a new string with all whitespace characters from both ends of the input string.

### Example

    >>\trim {  Hello World!   }<<

output

    >>Hello World!<<

### Exceptions

If the argument is not a string, an exception is thrown.


true
----

### Description

True.

### Input

none

### Output

Returns `true`.

### Example

    \true

output

    true

### Exceptions

none


u
-

### Description

Unicode character.

### Input

Takes one numerical argument.

### Output

Returns the Unicode character corresponding to the value of the argument.

### Example

    \u 0x61

output

    a

### Exceptions

If the argument is not numerical, an exception is thrown.


upcase
------

### Description

Makes all characters in a string upper case.

### Input

Takes a string argument.

### Output

Returns a new string containing only upper case characters.

### Example

    \upcase {Hello World!}

output

    HELLO WORLD!

### Exceptions

If the argument is not a string, an exception is thrown.


{}
--

### Description

The empty object.

### Input

none

### Output

Returns the empty object.

### Example

    \{}

output

    {}

### Exceptions

none
