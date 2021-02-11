package xyz.hyperreal.backslash

import scala.scalajs.js
import js.Dynamic.{global => g}

object File {

  private val fs = g.require("fs")

  def read(file: String): String = fs.readFileSync(file).toString

  def write(file: String, data: String): Unit = fs.writeFileSync(file, data)

  def readable(file: String): Boolean =
    try {
      fs.accessSync(file, fs.constants.R_OK)
      true
    } catch {
      case _: Exception => false
    }

  def writable(file: String): Boolean =
    try {
      fs.accessSync(file, fs.constants.W_OK)
      true
    } catch {
      case _: Exception => false
    }

}
