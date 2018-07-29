

\\number
------

## Description

Conversion to a number.

## Input

Takes a numerical or string argument.

## Output

If the argument is a string, parses it as a number and returns a numerical result.  If the argument is a number, it is returned as is.

## Example

    \+ \number "123" 1

output

    124

## Exceptions

If the argument is not a string or numerical, an exception is thrown.


\\range
-------

## Description

Creates a sequence ranging from one number to the other.  This command does not actually enumerate over all the numbers in the range to create the sequence, it creates an object that behaves as if it were that sequence (i.e. runs in O(1) time).

## Input

Takes two numerical arguments.

## Output

Returns the range as a sequence result.

## Example

    \range 3 6

output

    [3, 4, 5, 6]

## Exceptions

If the arguments are not numerical, an exception is thrown.


\\rem
---

## Description

Computes the remainder of two numbers.

## Input

Takes two numerical arguments.

## Output

Returns the remainder as a numerical result.

## Example

    \rem 8 3

output

    2

## Exceptions

If the arguments are not numerical, an exception is thrown.


\\remove
------

## Description

Removes every occurrence of a substring from a string.

## Input

Takes two string arguments.

## Output

Returns the second string with every occurrence of the first removed.

## Example

    \remove "rain" "I strained to see the train through the rain"

output

    I sted to see the t through the

## Exceptions

If the arguments are not strings, an exception is thrown.


\\removeFirst
-----------

## Description

Removes the first occurrence of a substring from a string.

## Input

Takes two string arguments.

## Output

Returns the second string with the first occurrence of the first removed.

## Example

    \removeFirst "rain" "I strained to see the train through the rain"

output

    I sted to see the train through the rain

## Exceptions

If the arguments are not strings, an exception is thrown.


\\replace
-------

## Description

Replaces every occurrence of a substring from a string.

## Input

Takes three string arguments.

## Output

Returns the third argument with every occurrence of the first replaced with the second.

## Example

    \replace "my" "your" "Take my protein pills and put my helmet on"

output

    Take your protein pills and put your helmet on

## Exceptions

If the arguments are not strings, an exception is thrown.


\\replaceFirst
------------

## Description

Replaces the first occurrence of a substring from a string.

## Input

Takes three string arguments.

## Output

Returns the third argument with the first occurrence of the first replaced with the second.

## Example

    \replaceFirst "my" "your" "Take my protein pills and put my helmet on"

output

    Take your protein pills and put my helmet on

## Exceptions

If the arguments are not strings, an exception is thrown.


\\reverse
-------

## Description

Reverses a sequence or string.

## Input

Takes a sequence or string argument.

## Output

Returns the sequence or string with all elements in reverse order.

## Example

    \seq {3 4 5} | reverse

output

    [5, 4, 3]

## Exceptions

If the argument is not a sequence or string, an exception is thrown.


\\round
-----

## Description

Rounds a number to a given scale (0 by default) according to the rounding mode given by the *rounding* configuration property (`HALF_EVEN` or "banker's rounding" is the default).

## Input

Takes one numerical argument, as well as an optional `scale` argument.

## Output

Returns the rounded value of the argument.

## Example

    \round 5.2 \round 5.6 \round 1.23 scale: 1 \round 1.26 scale: 1

output

    5 6 1.2 1.3

## Exceptions

If the argument is not numerical, an exception is thrown.


\\size
----

## Description

The size of a sequence, object or string.

## Input

Takes a sequence, object or string argument.

## Output

Returns the number of elements in the sequence, or the number of properties in the object, or the number of characters in the string.

## Example

    \seq {3 4 5} | size

output

    3

## Exceptions

If the argument is not a sequence or string, an exception is thrown.


\\slice
-----

## Description

A slice from a sequence or string.

## Input

Takes a numerical argument giving the start index of the slice, a numerical argument giving the end index of the slice (exclusive), and a sequence or string argument.

## Output

Returns a sub-sequence from a sequence or substring from a string.

## Example

    \seq {3 4 5 6 7} | slice 2 4

output

    [5, 6]

## Exceptions

If the argument is not a sequence or string, an exception is thrown.


\\sort
----

## Description

A sorted sequence.

## Input

Takes a sequence argument, an optional `on` argument specifying which property to sort on, and an optional `order` argument specifying the sort order.  The `order` argument can be either `asc` (the default) or `desc`.

## Output

Returns a sorted sequence in which elements are sorted according to their natural ordering.

## Example

    \seq {3 5 4 7 6 2 1} | sort

output

    [1, 2, 3, 4, 5, 6, 7]

## Exceptions

If the argument is not a sequence, an exception is thrown.


\\split
-----

## Description

Splits a string into a sequence of substrings around a separator.

## Input

Takes two string arguments: the separator (a regular expression), and the string to be split.

## Output

Returns the second string split into a sequence using the first as a separator regular expression.

## Example

    \split "\\s+" "This is a sentence"

output

    ["This", "is", "a", "sentence"]

## Exceptions

If the arguments are not strings, an exception is thrown.


\\string
------

## Description

Converts to a *display* string.  A *display* string is a readable string representation of something that's not a string.

## Input

Takes one argument of any kind.

## Output

Returns a new string displaying the contents of the argument in a readable manner.  If the argument is already a string, it is left unchanged.  Numbers appear as expected.

## Example

    \{a 3 b 4} | string

output

    {"a": 3, "b": 4}

## Exceptions

none


\\t
-

## Description

The tab character (\u0009).

## Input

none

## Output

Returns a string containing only the tab character.

## Example

    \t

output

```

```

## Exceptions

none


\\tail
----

## Description

Selects all elements other than the first of a sequence or string.

## Input

Takes a sequence or string argument.

## Output

Returns every element after the first element of the sequence or string.

## Example

    \seq {3 4 5} | tail

output

    [4, 5]

## Exceptions

If the argument is not a sequence or string, or the sequence is empty, an exception is thrown.


\\take
----

## Description

Takes a specified number of items from the head of a sequence or string.

## Input

Takes an integer argument and a string or sequence argument.

## Output

If the second argument is a string, returns a new string containing only the number of characters given by the first argument.  If the second argument is a sequence, returns a new sequence containing only the number of items given by the first argument.

## Example

    \take 2 \seq {3 4 5 6 7}

output

    [3, 4]

## Exceptions

If the first argument is not a number and the second is not a string or a sequence, an exception is thrown.


\\timestamp
---------

## Description

Converts a string or number into a timestamp.

## Input

Takes a string argument.

## Output

If the argument is a string, returns a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) object corresponding to the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) timestamp (date and time representation) input string.  If the argument is an integer, returns a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) instance at time zone UTC corresponding to the Unix epoch time in milliseconds given by the integer argument.

## Example

    \timestamp "2018-06-05T14:52:25Z" | date "MMMM d, y"

output

    June 5, 2018

## Exceptions

If the argument is not a string or an integer, an exception is thrown.


\\today
-----

## Description

The current date.

## Input

none

## Output

Returns a string representing the current date formatted according to the `today` configuration property.

## Example

    \today

output

    June 5, 2018

## Exceptions

none


\\trim
----

## Description

Removes all whitespace characters from both ends of a string.

## Input

Takes a string argument.

## Output

Returns a new string with all whitespace characters from both ends of the input string.

## Example

    >>\trim {  Hello World!   }<<

output

    >>Hello World!<<

## Exceptions

If the argument is not a string, an exception is thrown.


\\u
-

## Description

Unicode character.

## Input

Takes one numerical argument.

## Output

Returns the Unicode character corresponding to the value of the argument.

## Example

    \u 0x61

output

    a

## Exceptions

If the argument is not numerical, an exception is thrown.


\\upcase
------

## Description

Makes all characters in a string upper case.

## Input

Takes a string argument.

## Output

Returns a new string containing only upper case characters.

## Example

    \upcase {Hello World!}

output

    HELLO WORLD!

## Exceptions

If the argument is not a string, an exception is thrown.


\\{} (empty object)
--

## Description

The empty object.

## Input

none

## Output

Returns the empty object.

## Example

    \{}

output

    {}

## Exceptions

none
