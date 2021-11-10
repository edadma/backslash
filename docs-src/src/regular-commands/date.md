date
====

### Description

Formats a date.

### Input

Takes a string and a date (i.e. [TemporalAccessor](https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalAccessor.html)).

### Output

Returns the second argument formatted according to the pattern given by the first (see [Date Format Patterns](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)).

### Example

    \now | date "MMMM d, y"

output

    June 1, 2018

### Exceptions

If the arguments are not strings or the first is not a sequence or an object, an exception is thrown.
