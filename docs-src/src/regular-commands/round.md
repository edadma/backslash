round
=====

### Description

Rounds a number to a given scale (0 by default) according to the rounding mode given by the *rounding* configuration property (`HALF_EVEN` or "banker's rounding" is the default).

### Input

Takes one numerical argument, as well as an optional `scale` argument.

### Output

Returns the rounded value of the argument.

### Example

    \round 5.2 \round 5.6 \round 1.23 scale: 1 \round 1.26 scale: 1

output

    5 6 1.2 1.3

### Exceptions

If the argument is not numerical, an exception is thrown.
