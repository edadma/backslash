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


..
--

### Description

Creates a sequence ranging from one number to another.

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
