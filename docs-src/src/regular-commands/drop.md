drop
====

## Description

Drops items from the head of a sequence or string.

## Input

Takes an integer argument and a string or sequence argument.

## Output

If the second argument is a string, returns a new string skipping over the number of characters given by the first argument.  If the second argument is a sequence, returns a new sequence skipping over the number of items given by the first argument.

## Example

    \drop 2 \seq {3 4 5 6 7}

output

    [5, 6, 7]

## Exceptions

If the first argument is not a number and the second is not a string or a sequence, an exception is thrown.
