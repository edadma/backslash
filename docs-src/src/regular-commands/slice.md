slice
=====

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
