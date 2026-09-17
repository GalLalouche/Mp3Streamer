# Summary

I created this "actor" framework since Akka's was such a pain in the ass.

## Intuition

At its most basic core, an actor is a function from `Msg` to `Future[Result]` which uses a single
thread to read messages.
Here is a simplified implementation:

```scala
class Actor[Msg, Result](name: String, f: Msg => Result) {
  implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
  def !(msg: => Msg): Future[Result] = Future(f(msg))
}
```

## Usage

One should not implement actors directly.
Instead, use one of the many factory methods to build an actor and send messages to it.

```scala
val length = Actor[String, Int]("length", _.length)
val lengthFuture: Future[Int] = length ! "Hello, world!"
```

## Guarantees

1. Actors process one message at a time.
1. Actors process messages in order, on a dedicated thread.
1. Actors have an unlimited "mailbox".

More in-depth:

* Messages are passed by-name and are always evaluated on a thread. While it is not guaranteed that
  the *same* thread will be used for all messages (an out-of-work actor may terminate its thread to
  save resources), it *is* guaranteed that no two threads will be used concurrently.
* Message processing share a happened-before relationship (assuming the actor calls share them
  too, of course). Therefore, it is not required to synchronize or otherwise safely publish whatever
  data *only* the function uses to create the result see (any data seen by other threads needs to be
  safely published and synchronized, of course).
* While actors process messages in order, they do not necessarily wait for the previous message's
  Future to finish processing before starting the next one. The only thing they wait for the message
  processing function to finish. Since the function itself may be asynchronous, it is possible for
  the actor to start processing the next message before the previous message's Future has completed.

## Differences between simplified and classical actors

It is important to note that these actors (henceforth, simplified) are **not** actors in the
Akka/Erlang sense (henceforth, classical).
There are three main differences:

1. A classical actor is just a message processing machine, contrasted with simplified actors which
   are basically functions from their `Msg` to a `Future[Result]`.
1. Each simplified actor has its own OS thread (though it can be terminated and restarted as
   needed), contrasted with classical actors which share a common system. In other words, one cannot
   have hundreds of thousands of simplified actors running concurrently.
1. Simplified actors are vulnerable to deadlocks if an actor invokes itself (either directly or
   indirectly). This is because an actor cannot process two messages at the time, and often (but not
   always) try to completely finish the current message before processing the next one. In practice
   though, this shouldn't be an issue since actors built using the factory methods cannot invoke
   themselves asynchronously.
