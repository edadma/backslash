string
======

### Description

Converts to a *display* string.  A *display* string is a readable string representation of something that's not a string.

### Input

Takes one argument of any kind.

### Output

Returns a new string displaying the contents of the argument in a readable manner.  If the argument is already a string, it is left unchanged.  Numbers appear as expected.

### Example

    \{a 3 b 4} | string

output

    {"a": 3, "b": 4}

### Exceptions

none
