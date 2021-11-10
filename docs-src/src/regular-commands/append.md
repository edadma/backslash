append
======

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
