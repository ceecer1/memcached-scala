package com.kodeasync.memcached.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.kodeasync.memcached.manager.Transceiver.ResponseData

class LocalClient(remote: InetSocketAddress, handler: ActorRef)(implicit val system: ActorSystem) extends Actor {
  override def preStart(): Unit = {
    IO(Tcp) ! Connect(remote, options = Vector(SO.TcpNoDelay(false)))
  }

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      handler ! "connect failed"
      context stop self

    case c@Connected(remote, local) =>
      handler ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          handler ! "write failed"
        case Received(data) =>
          handler ! ResponseData(data)
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          handler ! "connection closed"
          context stop self
      }
  }

}

object LocalClient {
  def clientActor(handler: ActorRef, serverAddress: InetSocketAddress) = Props(classOf[LocalClient], serverAddress, handler)
}
