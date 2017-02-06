package com.kodeasync.memcached.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.slf4j.Logger
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import akka.util.ByteString
import com.kodeasync.memcached.manager.Transceiver.ResponseData

class LocalClient(remote: InetSocketAddress, handlerActor: ActorRef)(implicit val system: ActorSystem) extends Actor
  with ActorLogging {
  override def preStart(): Unit = {
    IO(Tcp) ! Connect(remote, options = Vector(SO.TcpNoDelay(false)))
  }

  context watch handlerActor

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      handlerActor ! "connect failed"
      context stop self

    case c@Connected(remote, local) =>
      handlerActor ! c
      val connection = sender()
      log.info("Connected !")
      connection ! Register(self)
      //val handlerActor = context.actorOf(handler(connection))
      //handlerActor ! c
      context become {
        case data: ByteString =>
          log.info("Sending data !")
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          handlerActor ! "write failed"
        case Received(data) =>
          println(data.utf8String)
          handlerActor ! ResponseData(data)
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          handlerActor ! "connection closed"
          context stop self
      }
  }

}

object LocalClient {
  def clientActor(handler: ActorRef, serverAddress: InetSocketAddress) = Props(classOf[LocalClient], serverAddress, handler)
}
