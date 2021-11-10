sort
====

### Description

A sorted sequence.

### Input

Takes a sequence argument, an optional `on` argument specifying which property to sort on, and an optional `order` argument specifying the sort order.  The `order` argument can be either `asc` (the default) or `desc`.

### Output

Returns a sorted sequence in which elements are sorted according to their natural ordering.

### Example

    \seq {3 5 4 7 6 2 1} | sort

output

    [1, 2, 3, 4, 5, 6, 7]

### Exceptions

If the argument is not a sequence, an exception is thrown.
