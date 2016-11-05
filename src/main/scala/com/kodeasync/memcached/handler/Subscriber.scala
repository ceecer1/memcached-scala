package com.kodeasync.memcached.handler

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy}
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.util.ByteString
import com.kodeasync.memcached.manager.Transceiver.ResponseData

/**
  * Created by shishir on 8/4/16.
  */
class Subscriber(transceiver: ActorRef) extends ActorSubscriber with ActorLogging {

  val requestStrategy = OneByOneRequestStrategy

  def receive = {
    case OnNext(msg: Array[Byte]) => {
      //log.info(s"Received ${msg.toString}")
      transceiver ! ResponseData(msg)
    }

    case OnComplete => {
      log.info("Data Stream Completed")
      //main ! GetPersistence
      context.system.terminate()
    }
    case OnError(err) => log.error(err, "Data Stream Error")
    case _ =>
  }

}

object Subscriber {
  def props(transceiver: ActorRef) = Props(classOf[Subscriber], transceiver)
}