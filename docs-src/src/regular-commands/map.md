map
===

Transforms a sequence by applying a lambda expression to all elements of the sequence.

## Input

Takes a lambda expression and a sequence as arguments.

## Output

Returns a new sequence with all elements transformed by the lambda expression.  If the lambda expression is a string, then it is treated as a short hand for `\. _ <string argument>`.

## Example

    \seq {3 4 5} | map \+ _ 2

output

    [5, 6, 7]

## Exceptions

If the second argument is not a sequence, an exception is thrown.
