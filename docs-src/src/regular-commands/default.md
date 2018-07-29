default
=======

## Description

Provides a default as a fallback in case a value doesn't exit.

## Input

Takes two arguments of any kind.

## Output

Returns the second argument (unchanged), unless it is `nil` in which case the "default" first value is returned.

## Example

    \price | default 2.99

output (assuming the variable `price` is *not* defined)

    2.99

## Exceptions

none
