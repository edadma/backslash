---
title: Regular Command Reference
---

Regular Commands
================

This page details all the built-in regular commands in lexicographical order.

[*](#)

[+](#-1)

[-](#-)

[..](#-2)


*
-

### Description

Multiplies two numbers returning the product.

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

Adds two numbers returning the sum, or concatenates two sequences or two strings or two objects, or appends or prepends an item to a sequence.

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

Creates a sequence ranging from one number to another.  This command does not actually enumerate over all the numbers in the range to create the sequence, it creates an object that behaves as if it were that sequence (i.e. runs in O(1) time).

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

Test for inequality.

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

Test whether one argument is less than the other.

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

Test whether one argument is less than or equal to the other.

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

Test for equality.

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


<
-

### Description

Test whether one argument is greater than the other.

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


>=
--

### Description

Test whether one argument is greater than or equal to the other.

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

Raises one number to the power of another.

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

Computes the absolute value.

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

Computes the *ceiling function*, maps the argument to the least integer greater than or equal to it.

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

Takes a string and a date (`java.time.temporal.TemporalAccessor`).

### Output

The second argument formatted according to the pattern given by the first (see [Date Format Patterns](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)).

### Example

    \now | date "MMMM d, y"

output

    June 1, 2018

### Exceptions

If the arguments are not strings or the first is not a sequence or an object, an exception is thrown.


default
-------
