package http4s

import cats.effect.IO
import com.google.inject.Singleton
import fs2.concurrent.Topic
import http4s.Http4sTopics.Result
import org.http4s.websocket.WebSocketFrame

import scala.collection.mutable

import common.rich.primitives.RichBoolean.richBoolean

@Singleton class Http4sTopics {
  private val map: mutable.Map[Class[_], Result] =
    mutable.Map[Class[_], Result]()
  def topic(clazz: Class[_]): IO[Result] = synchronized {
    if (map.contains(clazz).isFalse)
      Topic
        .apply[IO, WebSocketFrame]
        .map { topic =>
          map += clazz -> topic
          topic
        }
    else
      IO.pure(map(clazz))
  }
}

object Http4sTopics {
  type Result = Topic[IO, WebSocketFrame]
}
