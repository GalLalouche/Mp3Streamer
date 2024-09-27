package backend.logging

import java.util.{Collections, Properties}

import better.files.File
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.{asScalaIteratorConverter, mapAsScalaMapConverter}

import common.io.IODirectory
import common.rich.collections.RichTraversableOnce.richTraversableOnce

object ScribeConfigLoader {
  def go(): Unit = {
    val resources = getClass.getClassLoader.getResources("backend/logging")
    for {
      resourceDir <- Collections.list(resources).iterator.asScala.singleOpt.iterator
      if File(resourceDir).exists
      dir = IODirectory(resourceDir.getFile)
      file <- dir.files
      if file.extension == "conf"
    } {
      val props = new Properties
      props.load(file.inputStream)
      val conf = ConfigFactory.parseFile(file.file)
      for ((k, v) <- conf.root.asScala)
        ScribeUtils.setLevel(k, v.unwrapped.toString)
    }
  }
}
