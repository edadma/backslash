package xyz.hyperreal.backslash

import java.nio.file.{Files, Paths}

object File {

  def read(file: String): String = Files.readString(Paths.get(file))

  def write(file: String, data: String): Unit = Files.writeString(Paths.get(file), data)

  def readable(file: String): Boolean = {
    val path = Paths.get(file)

    Files.isReadable(path) && Files.isRegularFile(path)
  }

  def writable(file: String): Boolean = {
    val path = Paths.get(file)

    Files.createFile(path)
    Files.isWritable(path) && Files.isRegularFile(path)
  }

}
