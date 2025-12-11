package backend.logging

import java.util.{Collections, Properties}

import better.files.File
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

import common.io.IODirectory
import common.rich.collections.RichTraversableOnce.richTraversableOnce

object ScribeConfigLoader {
  def go(): Unit = {
    val resources = getClass.getClassLoader.getResources("backend/logging")
    // When running in production mode, this will also pull up all the resources from inside the
    // compiled JAR for some reason.
    val dirs = Collections
      .list(resources)
      .asScala
      .filterNot(_.getFile.contains(".jar"))
      .filterNot(_.getFile.contains("test-classes"))
    for {
      resourceDir <- dirs.singleOpt
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
