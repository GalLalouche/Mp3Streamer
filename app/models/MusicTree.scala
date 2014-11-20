package models

import java.io.File
import common.ValueTree
import common.rich.path.Directory
import common.rich.path.RichPath._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import common.rich.path.RichFile

trait MusicTree extends MusicFinder {
	collection.parallel.ForkJoinTasks
	private def buildNode(d: Directory, depth: Int = 0): ValueTree[File] = {
		val leaves = d.files.filter(extensions.contains(_))
		val dirs = if (depth > 1) d.dirs else d.dirs.par // shouldn't be too parallel
		val sons = dirs.map(buildNode(_, depth + 1))
		ValueTree(d.dir, leaves, sons.toList)
	}

	def getTree = {
		ValueTree(dir.dir, List(), genreDirs.par.map(buildNode(_)).toList)
	}

	override def toString = getTree.toString
}

object MusicTree {
	def apply(mf: MusicFinder) = {
		require(mf != null);
		new MusicTree { val dir = mf.dir; val subDirs = mf.subDirs; val extensions = mf.extensions }
	}

	def jsonify(tree: ValueTree[File]): JsObject = {
		import common.rich.path.RichPath._
		tree match {
			case _ if (tree.isLeaf) =>
				Json obj (
					"data" -> Json.obj(
						"title" -> tree.value.name,
						"attr" ->
							Json.obj("path" -> tree.value.path)
					)
				)
			case _ =>
				Json obj (
					"data" -> tree.value.name,
					"children" -> tree.sons.map(jsonify)
				)
		}

	}
}