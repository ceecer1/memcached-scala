package com.kodeasync.memcached.handler

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError}

import scala.collection.mutable

/**
  * Created by shishir on 8/4/16.
  */
class EventActorPublisher extends Actor with ActorPublisher[Exchange] with ActorLogging {

  var queue: scala.collection.mutable.Queue[Exchange] = mutable.Queue.empty

  def receive = {
    case m: Array[Byte] =>
      log.info(s"- message received and queued: ${m.toString}")
      queue.enqueue(Exchange(m))
      publish()

    case Request => publish()

    case Cancel =>
      log.info("- cancel message received")
      context.stop(self)

    case OnError(err: Exception) =>
      log.info("- error message received")
      onError(err)
      context.stop(self)

    case OnComplete =>
      log.info("- onComplete message received")
      onComplete()
      context.stop(self)
  }

  def publish() = {
    while (queue.nonEmpty && isActive && totalDemand > 0) {
      log.info("- message published")
      onNext(queue.dequeue())
    }
  }
}

object EventActorPublisher {
  def props = Props[EventActorPublisher]
}