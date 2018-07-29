join
====

## Description

Concatenates all elements of a sequence into a string.

## Input

Takes a string and a sequence.

## Output

Returns the concatenation of all elements of a sequence into a string, placing the given separator between each element.

## Example

    \join " - " \seq {1 2 3}

output

    1 - 2 - 3

## Exceptions

If the first argument is not a string and the second is not a sequence, an exception is thrown.
