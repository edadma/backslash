---
title: Regular Command Reference
---

Regular Commands
================


This page details all the built-in regular commands in lexicographical order.

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
