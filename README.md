Backslash
=========

[![Build Status](https://www.travis-ci.org/edadma/backslash.svg?branch=master)](https://www.travis-ci.org/edadma/backslash)
[![Build status](https://ci.appveyor.com/api/projects/status/h5b23n2vd0k4oh9q/branch/master?svg=true)](https://ci.appveyor.com/project/edadma/backslash/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/edadma/backslash/badge.svg?branch=master)](https://coveralls.io/github/edadma/backslash?branch=master)
[![License](https://img.shields.io/badge/license-ISC-blue.svg)](https://github.com/edadma/backslash/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/latest_release-v0.4.12-orange.svg)](https://github.com/edadma/backslash/releases/tag/v0.4.12)

*Backslash* is a string templating language written in [Scala](http://scala-lang.org). Backslash looks like [TeX](https://en.wikipedia.org/wiki/TeX) with the default delimiters (which can be changed), but it's not TeX. Backslash behaves like any other macro/templating language: it copies input text to output. Backslash is an attempt to create a dryer templating language that still allows you to copy HTML (or whatever you're using it for) verbatim. So, although Backslash is somewhat inspired by TeX, it shares very little in common with it except less typing. If your HTML doesn't containing any scripting then the "TeXish" delimiters (`\`, `{`, `}`) are usually fine. However, as in [Mustache](http://mustache.github.io/), delimiters can be changed anywhere in the input stream (except between command arguments).


Examples
--------

### Templating

Here's a typical Backslash template with looping and conditionals.

#### Template

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

#### Data

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

#### Output

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

### Library

This example program shows how to create a custom command to output an HTML unordered list, and also demonstrates a Backslash `\for` loop.

```scala
import scala.util.parsing.input.Position

import xyz.hyperreal.backslash._


object Example extends App {

  val input =
    """
      |<h2>Vaudeville Acts</h2>
      |<ol>
      |  \for \in act acts {
      |    <li>
      |      <h3>\act.name</h3>
      |      \list \act.members
      |    </li>
      |  }
      |</ol>
    """.trim.stripMargin
  val acts =
    List(
      Map(
        "name" -> "Three Stooges",
        "members" -> List( "Larry", "Moe", "Curly" )
      ),
      Map(
        "name" -> "Andrews Sisters",
        "members" -> List( "LaVerne", "Maxine", "Patty" )
      ),
      Map(
        "name" -> "Abbott and Costello",
        "members" -> List( "William (Bud) Abbott", "Lou Costello" )
      )
    )
  val customCommand =
    new Command( "list", 1 ) {
      def apply( pos: Position, rendered: Renderer, args: List[Any], context: AnyRef ) = {
        val list = args.head.asInstanceOf[List[String]]

        s"<ul>${list map (item => s"<li>$item</li>") mkString}</ul>"
      }
    }

  val parser = new Parser( Command.standard ++ Map("list" -> customCommand) )
  val renderer = new Renderer( parser, Map() )

  renderer.render( parser.parse(io.Source.fromString(input)), Map("acts" -> acts), Console.out )
}
```

This program prints

```html
<h2>Vaudeville Acts</h2>
<ol>

    <li>
      <h3>Three Stooges</h3>
      <ul><li>Larry</li><li>Moe</li><li>Curly</li></ul></li>

    <li>
      <h3>Andrews Sisters</h3>
      <ul><li>LaVerne</li><li>Maxine</li><li>Patty</li></ul></li>

    <li>
      <h3>Abbott and Costello</h3>
      <ul><li>William (Bud) Abbott</li><li>Lou Costello</li></ul></li>

</ol>
```

### Executable

This next example shows how to use *Backslash* as an executable from the command line.

```bash
echo "testing \join \v \", \"" | java -jar backslash-0.4.12.jar -j "{v: [\"one\", \"two\", \"three\"]}" --
```

The above command prints

    testing one, two, three


Usage
-----

### Library

Use the following definition to use Backslash in your Maven project:

```xml
<repository>
  <id>hyperreal</id>
  <url>https://dl.bintray.com/edadma/maven</url>
</repository>

<dependency>
  <groupId>xyz.hyperreal</groupId>
  <artifactId>backslash</artifactId>
  <version>0.4.12</version>
</dependency>
```

Add the following to your `build.sbt` file to use Backslash in your SBT project:

```sbt
resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies += "xyz.hyperreal" %% "backslash" % "0.4.12"
```

### Executable

An executable can be downloaded from [here](https://dl.bintray.com/edadma/generic/backslash-0.4.12.jar). *You do not need* the Scala library for it to work because the JAR already contains all dependencies. You just need Java 8+ installed.

Run it as a normal Java executable JAR with the command `java -jar backslash-0.4.12.jar <template>` in the folder where you downloaded the file, where *template* is the name of the template file to be rendered.

Building
--------

### Requirements

- Java 8
- SBT 1.1.5+
- Scala 2.12.6+

### Clone and Assemble Executable

```bash
git clone git://github.com/edadma/backslash.git
cd backslash
sbt assembly
```

The command `sbt assembly` also runs all the unit tests.


License
-------

ISC © 2018 Edward A. Maxedon, Sr.