package common

import java.io.File

// TODO move to ScalaCommon
object RichClass {
  implicit class richClass[A](private val $: Class[A]) extends AnyVal {
    def getFile(path: String) = new File($.getResource(path).getFile)
  }
}
