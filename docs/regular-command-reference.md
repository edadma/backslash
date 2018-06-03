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

    -1

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

Takes one string argument.

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

    hello world!

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

    `false`

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

If the argument is not numerical, an exception is thrown.


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
