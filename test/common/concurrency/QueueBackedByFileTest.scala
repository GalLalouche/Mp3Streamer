package common.concurrency

import java.util.concurrent.Semaphore

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.RichJson._
import common.io.MemoryRoot
import common.json.Jsonable
import common.rich.RichFuture._
import common.rich.collections.RichTraversableOnce._
import org.scalatest.{FreeSpec, OneInstancePerTest}
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class QueueBackedByFileTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private implicit val c: ExecutionContext = TestConfiguration(_ec = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = {
      val t = new Thread(runnable)
      t setDaemon true
      t.start()
    }
    override def reportFailure(cause: Throwable): Unit = ???
  })
  private val file = new MemoryRoot().addFile("foo")

  private val persons = mutable.Buffer[Person]()
  private case class Person(name: String, age: Int)
  private object Person {
    implicit object Jsonable extends Jsonable[Person] {
      override def jsonify(t: Person) = Json.obj("name" -> t.name, "age" -> t.age)
      override def parse(json: JsValue) = Person(json str "name", json int "age")
    }
  }

  private def withConsumer[T: Jsonable](f: T => Unit) = new QueueBackedByFile(f, file)
  private val $ = new QueueBackedByFile(persons.+=, file)
  private val p1 = Person("foo", 42)
  private val p2 = Person("bar", 54)
  private val p3 = Person("bazz", 666)
  "!" - {
    "consumes input" in {
      $.!(p1).get
      persons.single shouldReturn p1
    }
    "If it fails, nothing is written to file" in {
      val consumer: Person => Unit = _ => throw new Exception
      val $ = withConsumer(consumer)
      $.!(p1).getFailure
      persons shouldBe 'empty
      QueueBackedByFile.reload(consumer, file).get // If it tries to consume again, an exception will be thrown
      persons shouldBe 'empty
    }
  }
  "reload" - {
    val semaphore = new Semaphore(0)
    def stuckOnFirst[T](onRest: (T => Unit)*): T => Unit = stuckOnN(1, onRest: _*)
    def stuckOnN[T](n: Int, onRest: (T => Unit)*): T => Unit = {
      @volatile var count = 0
      val iterator = onRest.iterator
      person => {
        val shouldBeStuck = QueueBackedByFileTest.this.synchronized {
          val $ = count < n
          count += 1
          $
        }
        semaphore.release()
        if (shouldBeStuck) while (true) {} else iterator.next()(person)
      }
    }
    "If it didn't finish, tries again" in {
      val consumer = stuckOnFirst(persons.+=)
      val $ = withConsumer(consumer)
      $ ! p1
      semaphore.acquire()
      QueueBackedByFile.reload(consumer, file).get
      persons.single shouldReturn p1
      // Makes sure the file is cleared after success
      QueueBackedByFile.reload(consumer, file).get
      persons.single shouldReturn p1
    }
    "reload throws an exception" in {
      // Complex scenario time: Success, exception, success, exception, success. After 3 calls (
      // success then fails, succeed then fails, succeeds), all data should be processed.
      val list = List((p1, "foo"), (p1, "bar"), (p2, "bazz"))
      val personsAndStrings = mutable.Buffer[(Person, String)]()
      val e1 = new Exception("foobar")
      val e2 = new Exception("foobar")
      val consumer =
        stuckOnN[(Person, String)](list.size, personsAndStrings.+=, _ => throw e1, personsAndStrings.+=, _ => throw e2, personsAndStrings.+=)
      val $ = withConsumer(consumer)
      for (e <- list) $ ! e
      semaphore acquire list.size
      QueueBackedByFile.reload(consumer, file).getFailure shouldReturn e1
      personsAndStrings.toList shouldReturn list.take(1)
      QueueBackedByFile.reload(consumer, file).getFailure shouldReturn e2
      personsAndStrings.toList shouldReturn list.take(2)
      QueueBackedByFile.reload(consumer, file).get
      personsAndStrings.toList shouldReturn list.take(3)
    }
  }
}
