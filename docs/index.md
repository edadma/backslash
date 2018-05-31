---
title: Backslash Reference Manual
---

Backslash Reference Manual
==========================


Syntax
------

Being a macro/templating language, Backslash basically copies input to output, while performing the appropriate character decoding and encoding, respectively. The basic character copying behaviour is *escaped* or interrupted when one of three *delimiters* or special separator character sequences is encountered, namely: the *control sequence* escape, the beginning of a *group*, and the end of a *group*.


### Control Sequences

A *control sequence* is a way of interrupting the normal character copying behaviour in order to tell Backslash to do something else at that point in the character stream. Once the action indicated by the control sequence is complete, character copying resumes with the characters immediately following the control sequence.  Control sequences begin with a `\` (backslash) character (whence the name of this templating language) followed by the name of the control sequence.

There are five categories into which control sequences can be grouped, depending on how they need to be handled during *parsing* and also during *rendering*. *Parsing* refers to how input stream characters are treated, and *rendering* refers to how the output stream is produced.  The five categories are discussed below.


#### Regular Commands

*Regular commands* or *functions* constitute the largest category of control sequences and cover a wide range of uses.  They are called *regular* because they are all handled the same way during parsing and rendering.  Some functions need to be supplied with special input, known as *arguments* in order to produce an output, and others can produce their output without requiring any arguments.