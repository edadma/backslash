<=
===

### Description

Tests whether one argument is less than or equal to the other.

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
