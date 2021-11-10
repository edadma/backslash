split
=====

### Description

Splits a string into a sequence of substrings around a separator.

### Input

Takes two string arguments: the separator (a regular expression), and the string to be split.

### Output

Returns the second string split into a sequence using the first as a separator regular expression.

### Example

    \split "\\s+" "This is a sentence"

output

    ["This", "is", "a", "sentence"]

### Exceptions

If the arguments are not strings, an exception is thrown.
