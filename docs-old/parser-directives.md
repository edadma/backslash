---
title: Parser Directive Reference
---

Parser Directives
=================


def
---

### Description

Define a macro.

### Syntax

    def *name* *parameters*... { ...*body*... }

where *name* is the name of the macro definition and *parameters* is a series of parameter names (possibly none) separated by space, and *body* is the material being assigned to *name*.  The parameters are referenced within the body of the definition as if they were variables, though their existence is local to the macro.

### Example

```html
\def h level heading {<h\level>\heading</h\level>}

\h1 Overview
<p>This is the overview.</p>

\h2 {First Subsection}
<p>Let's read something else now.</p>
```

output

```html
<h1>Overview</h1>
<p>This is the overview.</p>

<h2>First Subsection</h2>
<p>Let's read something else now.</p>
```

### Exceptions

The body of a macro definition must be surrounded by [group](./#grouping) delimiters.  If no, an exception is thrown.
