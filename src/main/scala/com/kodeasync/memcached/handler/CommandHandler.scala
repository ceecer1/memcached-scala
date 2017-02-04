package com.kodeasync.memcached.handler

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.kodeasync.memcached.manager.Transceiver
import com.kodeasync.memcached.manager.Transceiver.{RequestQueue, RequestedData}

import scala.collection.immutable.Queue

/**
  * Created by shishir on 11/3/16.
  */
class CommandHandler()(implicit val system: ActorSystem) extends Actor with ActorLogging {



  import CommandHandler._
  var sendTo: Option[ActorRef] = None

  def receive = {
    case s: SetCommand => {
      sendTo = Some(sender())
      val commandString = "set " + s.key + " 0 " + s.expiry + " " + s.bytes.length + " \r\n"
      val crlfBytes = ByteString("\r\n")
      val commandBytes = ByteString(commandString)
      val requestData1 = RequestedData(commandBytes)
      val requestedData2 = RequestedData(ByteString(s.bytes))
      val crlfRequest = RequestedData(crlfBytes)
      val requestQueue = RequestQueue(Queue(requestData1, requestedData2, crlfRequest))
      val trans = context.actorOf(Props(new Transceiver()(system)))
      trans ! requestQueue
    }

    case g: GetCommand => {
      sendTo = Some(sender())
      val commandString = "get " + g.key + " \r\n"
      val commandBytes = ByteString(commandString)
      val requestData1 = RequestedData(commandBytes)
      val requestQueue = RequestQueue(Queue(requestData1))
      val trans = context.actorOf(Props(new Transceiver()(system)))
      trans ! requestQueue
    }

    case d: DeleteCommand => {
      sendTo = Some(sender())
      val commandString = "delete " + d.key + " \r\n"
      val commandBytes = ByteString(commandString)
      val requestData1 = RequestedData(commandBytes)
      val requestQueue = RequestQueue(Queue(requestData1))
      val trans = context.actorOf(Props(new Transceiver()(system)))
      trans ! requestQueue
    }

    case r: CommandResponse => {
      //val string = new String(r.data, "UTF-8")
      //println(s"CommandResponse is : ${string}")
      println("printed at CommandHandler")
      println(r.data.utf8String)
      sendTo.get ! r
    }
  }

}

object CommandHandler {
  case class SetCommand(key: String, expiry: Int, bytes: Array[Byte])
  case class GetCommand(key: String)
  case class DeleteCommand(key: String)
  case object CrlfCommand 
  case object DataCommand

  case class CommandResponse(data: ByteString)

  def props = Props(classOf[CommandHandler])
}
