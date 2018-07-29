filter
======

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
