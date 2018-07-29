include
=======

## Description

Reads a file as a Backslash template.

## Input

Takes a string argument as the path to the file to be included.  The path is relative to the `include` configuration property.

## Output

Returns the rendered Backslash file.

## Example

    \include "sections/header.bac"

output


## Exceptions

An exception may be thrown during the rendering of the included file.
