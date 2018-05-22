Backslash
=========

[![Build Status](https://www.travis-ci.org/edadma/backslash.svg?branch=master)](https://www.travis-ci.org/edadma/backslash)
[![Coverage Status](https://coveralls.io/repos/github/edadma/backslash/badge.svg?branch=master)](https://coveralls.io/github/edadma/backslash?branch=master)
[![License](https://img.shields.io/badge/license-ISC-blue.svg)](https://github.com/edadma/backslash/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/latest_release-v0.1-orange.svg)](https://github.com/edadma/backslash/releases/tag/v0.1)

*Backslash* is a string templating language written in [Scala](http://scala-lang.org).


Examples
--------

### Library

This example program shows how to create a custom tag to output an HTML unordered list, and also demonstrates a Liquid `for` loop.

```scala
import scala.util.parsing.input.Position

import xyz.hyperreal.backslash._


object Example extends App {

  val input =
    """
      |<h2>Vaudeville Acts</h2>
      |<ol>
      |  \for \in act \acts {
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
echo "testing \join \v \", \"" | java -jar backslash-0.1.jar -j "{v: [\"one\", \"two\", \"three\"]}" --
```

The above command prints

    testing one, two, three


Usage
-----

Use the following definition to use Backslash in your Maven project:

```xml
<repository>
  <id>hyperreal</id>
  <url>https://dl.bintray.com/edadma/maven</url>
</repository>

<dependency>
  <groupId>xyz.hyperreal</groupId>
  <artifactId>backslash</artifactId>
  <version>0.1</version>
</dependency>
```

Add the following to your `build.sbt` file to use Backslash in your SBT project:

```sbt
resolvers += "Hyperreal Repository" at "https://dl.bintray.com/edadma/maven"

libraryDependencies += "xyz.hyperreal" %% "backslash" % "0.1"
```

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

ISC © 2018 Edward Maxedon