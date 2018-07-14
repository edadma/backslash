---
title: Reference Manual
---


Overview
========

Backslash is a string templating language written in Scala. Backslash looks like TeX with the default delimiters (which can be changed), but it's not TeX. Backslash behaves like any other macro/templating language: it copies input text to output. Backslash is an attempt to create a dryer templating language that still allows you to copy HTML (or whatever you're using it for) verbatim. So, although Backslash is somewhat inspired by TeX, it shares very little in common with it except less typing. If your HTML doesn't containing any scripting then the "TeXish" delimiters (`\`, `{`, `}`) are usually fine. However, as in [Mustache](http://mustache.github.io/), delimiters can be changed anywhere in the input stream (except between command arguments).


Example
=======

Here's a typical Backslash template with looping and conditionals.

### Template

```html
<h3>Products</h3>

<ul>
  \for products {
    <li>\name&emsp;$\price&emsp;
      \if inStock {
        <a href="#">Buy It!</a>
      } \else {
        Out of stock.
      }
    </li>
  }
</ul>
```

### Data

```json
{
  "products": [
    {
      "name": "RCA 32\u2033 ROKU SMART TV",
      "price": 207.00,
      "inStock": true
    },
    {
      "name": "LG 55UK6300",
      "price": 1098.00,
      "inStock": false
    }
  ]
}
```

### Output

```html
<h3>Products</h3>

<ul>

    <li>RCA 32″ ROKU SMART TV&emsp; $207.00&emsp;

        <a href="#">Buy It!</a>

    </li>

    <li>LG 55UK6300&emsp; $1098.00&emsp;

        Out of stock.

    </li>

</ul>
```


Syntax
======

Being a macro/templating language, Backslash basically copies input to output, while performing the appropriate character decoding and encoding, respectively. The basic character copying behaviour is *escaped* or interrupted when one of three *delimiters* or special separator character sequences is encountered, namely: the *control sequence* escape, the beginning of a *group*, and the end of a *group*.


Control Sequences
-----------------

A *control sequence* is a way of interrupting the normal character copying behaviour in order to tell Backslash to do something else at that point in the input stream. Once the action indicated by the control sequence is complete, character copying resumes with the characters immediately following the control sequence, skipping over any which space right after the name of the control sequence.  Control sequences begin with a `\` (backslash) character (whence the name of this language) followed by the name of the control sequence.

There are five categories into which control sequences can be grouped, depending on how they need to be handled during *parsing* and also during *rendering*. *Parsing* refers to how input stream characters are treated, and *rendering* refers to how the output stream is produced.  The five categories are discussed below.


### Regular Commands

*Regular commands* or *functions* constitute the largest category of control sequences and cover a wide range of uses.  They are called *regular* because they are all handled the same way during parsing and rendering.  Some functions need to be supplied with special input, known as *arguments* (see [Regular Arguments](./#regular-arguments)) in order to produce an output, and others can produce their output without requiring any arguments.  The [Regular Command Reference](./regular-command-reference.html) contains information on all the built-in regular commands.

Here are some examples.

#### Without Arguments

Here is an example of a regular command that doesn't take any arguments:

    The current time is \now.

which outputs

    The current time is 2018-05-31T19:39:03.489-04:00[America/Montreal].

The `.` following the `\now` control sequence just gets copied to the output.


#### With Arguments

Next is an example of a regular command that takes an argument that is a string of characters and transforms it into another string of characters:

    \markdown {this is a __boring__ *example*}

which outputs

```html
<p>this is a <strong>boring</strong> <em>example</em></p>
```

Not all commands take character string arguments.  Here's an example of a command that takes two numerical arguments:

    three plus four is \+ 3 4

which outputs

    three plus four is 7


### Macros

A snippet of a template that is used in multiple places within the same file can be given a name and formal parameters so that it can be reused by simply referring to it as a control sequence.  These are called macro definitions.  Macros are discussed in the section dealing with the [`def` command](./parser-directives.html#def), which is the command that defines a macro.


### Special Commands

Special commands are control sequences the need to be parsed in some special way.  They can naturally be sub-divided into three categories.  [Flow control commands](./flow-control-commands.html) can alter the normal, sequential way in which input is transformed into output.  The [Logical connectives](./logical-connectives.html) are usually used with the flow control commands to do basic logic.  And finally, the [Miscellaneous special commands](./miscellaneous-special-commands.html) are the ones that don't fall into any special category.

### Parser Directives

The [Parser directives](./parser-directives.html) are control sequences that affect the parser in come way but don't produce any output.

### Variable References

Backslash has a facility for assigning names to data of any kind so that the data can then be inserted into the output stream or used as an argument to a command at a later point.  These are called *variables*.  A variable reference is a way of recalling the contents of a variable.  This is done by referring to the variable name in a control sequence.  Here's an example of a variable being assigned and then used.

    \set UnixEpoch {\timestamp 0 | date "MMMM d, y 'at' HH:mm O"}

    The Unix Epoch begins on \UnixEpoch.

output

    The Unix Epoch begins on January 1, 1970 at 00:00 GMT.

In the above example, the variable `UnixEpoch` is assigned a string representing the start of what is known as the "Unix Epoch" which is later which used in a sentence.

A variable may contain an object which means that there needs to be a way of accessing the properties within the object.  Variable reference can, therefore, make use of the well known "dotted" notation.  For example,

    \set product \{name "Nice TV" price 1049.00}

    The product name is \product.name.

output

    The product name is Nice TV.

A dot at the end of a variable reference is not considered to be part of it, it just get's copied to the output stream.  A variable reference may not begin with a dot nor can there be two dots in a row.  A variable reference may begin with an underscore (`_`) or letter, and may continue with any combination of underscores, letters and dots.  A variable reference may not contain any digits.

Grouping
--------


Commands Arguments
------------------


### Regular Arguments


### Expression Arguments


### Variable Arguments


Piping
------

