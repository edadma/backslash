escapeOnce
==========

## Description

Replaces characters in a string with HTML entity equivalents while avoiding any HTML entities that may already be present.

## Input

Takes one string argument.

## Output

Returns a new string with certain characters changed to their HTML entity equivalents leaving existing HTML entities unchanged.

## Example

    \escapeOnce {a < b &lt; c}

output

    a &lt; b &lt; c

## Exceptions

If the argument is not a string, an exception is thrown.
