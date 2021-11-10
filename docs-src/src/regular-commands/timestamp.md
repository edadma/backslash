timestamp
=========

### Description

Converts a string or number into a timestamp.

### Input

Takes a string argument.

### Output

If the argument is a string, returns a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) object corresponding to the [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) timestamp (date and time representation) input string.  If the argument is an integer, returns a [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html) instance at time zone UTC corresponding to the Unix epoch time in milliseconds given by the integer argument.

### Example

    \timestamp "2018-06-05T14:52:25Z" | date "MMMM d, y"

output

    June 5, 2018

### Exceptions

If the argument is not a string or an integer, an exception is thrown.
