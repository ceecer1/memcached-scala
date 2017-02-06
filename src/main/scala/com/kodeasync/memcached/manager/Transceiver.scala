package com.kodeasync.memcached.manager

import java.net.InetSocketAddress
import javax.xml.bind.Unmarshaller.Listener

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.io.Tcp.Connected
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.kodeasync.memcached.client.LocalClient
import com.kodeasync.memcached.handler.CommandHandler.CommandResponse

import scala.collection.immutable.Queue

/**
  * Created by shishir on 11/3/16.
  */
class Transceiver()(implicit val system: ActorSystem) extends Actor with ActorLogging {

  import Transceiver._

  val client = context.actorOf(Props(new LocalClient(new InetSocketAddress("192.168.99.100", 11211), self)))

  var listener: Option[ActorRef] = None
  //use become unbecome and change context after message is sent/received
  def receive = request

  def request: Receive = {

    case req: RequestQueue => {
      listener = Some(sender())
      val reque = req.queue
      val (head, newQueue) = reque.dequeue
      client ! head.data
      if(!newQueue.isEmpty) {
        self ! RequestQueue(newQueue)
      } else {
        context.become(response)
      }
    }

  }

  def response: Receive = {
    case res: ResponseData => {
      listener.get ! CommandResponse(res.data)
      //sender() ! "close"
      //context.stop(self)
    }

    case c@Connected(remote, local) =>
      println(s"Connnected -> remote: $remote, local: $local")


    case f@("connect failed" | "connection closed" | "write failed") =>
      println(f)

  }


}

object Transceiver {

  case class RequestedData(data: ByteString)
  case class RequestQueue(queue: Queue[RequestedData])
  case class ResponseData(data: ByteString)

  def props = Props(classOf[Transceiver])
}

/*publisher ! ("set duck 0 900 6\r\n").getBytes
      publisher ! ("ducker\r\n").getBytes
      publisher ! ("get mykey \r\n").getBytes
      publisher ! ("get app \r\n").getBytes
      publisher ! ("get query \r\n").getBytes*/