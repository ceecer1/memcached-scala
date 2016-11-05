package com.kodeasync.memcached.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.kodeasync.memcached.manager.Transceiver.{RequestQueue, RequestedData}
import scala.collection.immutable.Queue

/**
  * Created by shishir on 11/3/16.
  */
class CommandHandler(transceiver: ActorRef) extends Actor with ActorLogging {

  import CommandHandler._
  var sendTo: Option[ActorRef] = None

  def receive = {
    case s: SetCommand => {
      sendTo = Some(sender())
      val commandString = "set " + s.key + " 0 " + s.expiry + " " +s.bytes.length
      val crlfBytes = "\r\n".getBytes
      val commandBytes = commandString.getBytes
      val requestData1 = RequestedData(commandBytes)
      val requestedData2 = RequestedData(s.bytes)
      val crlfRequest = RequestedData(crlfBytes)
      val requestQueue = RequestQueue(Queue(requestData1, crlfRequest, requestedData2))
      transceiver ! requestQueue
    }

    case g: GetCommand => {
      sendTo = Some(sender())
      val commandString = "get " + g.key
      val commandBytes = commandString.getBytes
      val requestData1 = RequestedData(commandBytes)
      val requestQueue = RequestQueue(Queue(requestData1))
      transceiver ! requestQueue
    }

    case d: DeleteCommand => {
      sendTo = Some(sender())
      val commandString = "delete " + d.key
      val commandBytes = commandString.getBytes
      val requestData1 = RequestedData(commandBytes)
      val requestQueue = RequestQueue(Queue(requestData1))
      transceiver ! requestQueue
    }

    case r: CommandResponse => {
      //val string = new String(r.data, "UTF-8")
      //println(s"CommandResponse is : ${string}")
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

  case class CommandResponse(data: Array[Byte])

  def props(transceiver: ActorRef) = Props(classOf[CommandHandler], transceiver)
}