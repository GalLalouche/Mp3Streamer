package models

import java.io.File

import common.ValueTree
import common.rich.path.Directory
import common.rich.path.RichPath._
import controllers.RealLocations
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsObject, Json}

object MusicTree {
  private val mf = RealLocations
  collection.parallel.ForkJoinTasks
  private def buildNode(d: Directory, depth: Int = 0): ValueTree[File] = {
    val leaves = d.files.filter(mf.extensions.contains)
    val dirs = if (depth > 1) d.dirs else d.dirs.par // shouldn't be too parallel
    val sons = dirs.map(buildNode(_, depth + 1))
    ValueTree(d.dir, leaves, sons.toList)
  }

  def getTree: ValueTree[File] = ValueTree(mf.dir.dir, List(), mf.genreDirs.map(_.dir).par.map(buildNode(_)).toList)

  override def toString = getTree.toString

  def jsonify(tree: ValueTree[File]): JsObject = {
    tree match {
      case _ if tree.isLeaf && tree.value.exists =>
        Json obj (
          "data" -> Json.obj(
            "title" -> tree.value.name,
            "attr" ->
              Json.obj("path" -> tree.value.path)
          )
          )
      case _ =>
        Json obj(
          "data" -> tree.value.name,
          "children" -> tree.sons.map(jsonify)
          )
    }

  }
}
