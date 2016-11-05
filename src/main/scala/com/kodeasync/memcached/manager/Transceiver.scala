package com.kodeasync.memcached.manager

import javax.xml.bind.Unmarshaller.Listener

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.ByteString
import com.kodeasync.memcached.handler.CommandHandler.CommandResponse

import scala.collection.immutable.Queue

/**
  * Created by shishir on 11/3/16.
  */
class Transceiver(publisher: ActorRef) extends Actor with ActorLogging {

  import Transceiver._

  var listener: Option[ActorRef] = None
  //use become unbecome and change context after message is sent/received
  def receive = request

  def request: Receive = {

    case req: RequestQueue => {
      listener = Some(sender())
      val reque = req.queue
      val (head, newQueue) = reque.dequeue
      publisher ! head.data
      if(newQueue.isEmpty) {
        self ! Crlf
      } else {
        self ! RequestQueue(newQueue)
      }
    }

    case Crlf => {
      publisher ! ("\r\n").getBytes
      context.become(response)
    }

  }

  def response: Receive = {
    case res: ResponseData => {
      listener.get ! CommandResponse(res.data)
      context.become(request)
    }

  }


}

object Transceiver {

  case class RequestedData(data: Array[Byte])
  case class RequestQueue(queue: Queue[RequestedData])
  case class ResponseData(data: Array[Byte])
  case object Crlf

  def props(publisher: ActorRef) = Props(classOf[Transceiver], publisher)
}

/*publisher ! ("set duck 0 900 6\r\n").getBytes
      publisher ! ("ducker\r\n").getBytes
      publisher ! ("get mykey \r\n").getBytes
      publisher ! ("get app \r\n").getBytes
      publisher ! ("get query \r\n").getBytes*/