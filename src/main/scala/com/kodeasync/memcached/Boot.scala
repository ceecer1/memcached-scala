package com.kodeasync.memcached

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{BidiFlow, Flow, Sink, Source, Tcp}
import akka.util.ByteString
import com.kodeasync.memcached.handler.{CommandHandler, EventActorPublisher, Exchange, Subscriber}
import com.kodeasync.memcached.manager.Transceiver
import com.kodeasync.memcached.util.{Connector, MemcachedCache}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

/**
  * Created by shishir on 8/4/16.
  */
object Boot {

  val logger = LoggerFactory.getLogger(Boot.getClass)

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("memcached-scala", config)
  implicit val ec = system.dispatcher

  val publisherActorRef = system.actorOf(Props[EventActorPublisher])
  val pub = ActorPublisher[Exchange](publisherActorRef)

  val transceiverRef = system.actorOf(Props(new Transceiver(publisherActorRef)))
  val commandHandlerRef = system.actorOf(Props(new CommandHandler(transceiverRef)))

  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {

    type Message = Array[Byte]
    val host = Connector.address.host
    val port = Connector.address.port

    val deserialize:ByteString => Message = _.toArray
    val serialize:Message => ByteString = message => ByteString(message)

    val incoming:Flow[ByteString, Message, _] = Flow fromFunction deserialize
    val outgoing:Flow[Message, ByteString, _] = Flow fromFunction serialize

    val protocol = BidiFlow.fromFlows(incoming, outgoing)

    def source: Source[Message, _] = Source.fromPublisher(pub).map(e => e.bArray)

    val sink: Sink[Message, _] = Sink.actorSubscriber[Message](Subscriber.props(transceiverRef))

    Tcp()
      .outgoingConnection(host, port)
      .join(protocol).async
      .runWith(source, sink)


    val cache = new MemcachedCache(commandHandlerRef)

    //TODO before running this test
    //In this memcached test application, only 1 of the following commented lines will work during testing.
    //If we try to run more than 1 line, then the results won't come
    //This is because of the use of single akka stream over TCP channel and I haven't implemented message ordering
    // in the current code
    //I will be developing this a lot better as a reusable library.

    //val s = new State("state", "NSW", 31)
    //cache.set[Person]("state", s)

    //cache.set[String]("test", "gold")
    //cache.get[Person]("person").map(f => println(f.get.address))
    //cache.get[String]("dump").map(x => println(x.get))
    //cache.delete("test")

  }

}
