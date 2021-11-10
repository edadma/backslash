range
=====

### Description

Creates a sequence ranging from one number to the other.  This command does not actually enumerate over all the numbers in the range to create the sequence, it creates an object that behaves as if it were that sequence (i.e. runs in O(1) time).

### Input

Takes two numerical arguments.

### Output

Returns the range as a sequence result.

### Example

    \range 3 6

output

    [3, 4, 5, 6]

### Exceptions

If the arguments are not numerical, an exception is thrown.
