package common.concurrency

import common.io.FileRef
import common.json.{Jsonable, ToJsonableOps}
import common.rich.RichFuture._
import common.rich.RichT._
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

/**
* Saves all data to a file and only removes datum upon completion. By using the companion reload method, it's
* possible to reload all previous data on application startup and try to consume them.
* In order to be persistent, the input is some Jsonable T.
* Note that the tasks are removed regardless of the future's success result. However, if any task fails during
* the reload phase, then the entire reload fails, the file remains unchanged, i.e., you can reload again.
*/
class QueueBackedByFile[T: Jsonable](consumer: T => Any, outputFile: FileRef) extends ToJsonableOps
    with FutureInstances with ToBindOps {
  require(outputFile.exists)

  // The private methods use JSON primitives to avoid needless serializing and deserializing.
  private def getSavedTasks: JsArray = outputFile.readAll match {
    case "" => JsArray.empty
    case s => Json.parse(s).as[JsArray]
  }
  private def save(array: JsArray): Unit = outputFile write array.toString

  private def append(json: JsValue): Unit = save(getSavedTasks :+ json)
  private def remove(json: JsValue): Unit = {
    val set = getSavedTasks.value.toSet
    assert(set(json))
    (set - json).toSeq |> JsArray.apply |> save
  }

  /**
  * Submits a new datum to be consumed. If the datum was not was not completed by the time the application shut down, then it is
  * saved persistently, to be reloaded later. Note that the success of the consumption is irrelevant; if compeleted it will be
  * removed regardless. Client classes need to re-invoke this method on failure if desired.
  */
  def !(t: => T)(implicit ec: ExecutionContext): Future[_] = synchronized {
    val json = t.jsonify
    append(json)
    Future(consumer(t)) consumeTry remove(json).const
  }
}

object QueueBackedByFile extends ToJsonableOps {
  /**
  * Tries to first consume all data saved in the file; returns a new clear instance when completed successfully.
  * If any consumption fails, stops consumption and returns a failed future; the failed datum and all remaining
  * data are not removed from the file.
  */
  def reload[T: Jsonable](consumer: T => Unit, outputFile: FileRef)
      (implicit ec: ExecutionContext): Future[QueueBackedByFile[T]] = Future {
    val $ = new QueueBackedByFile(consumer, outputFile)
    $.getSavedTasks.value foreach (taskJson => {
      consumer(taskJson.parse[T])
      $ remove taskJson
    })
    $
  }
}
