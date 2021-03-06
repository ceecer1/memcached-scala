package com.kodeasync.memcached

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{BidiFlow, Flow, Sink, Source, Tcp}
import akka.util.ByteString
import com.kodeasync.memcached.client.LocalClient
import com.kodeasync.memcached.handler.CommandHandler
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
  implicit val materializer = ActorMaterializer()


  def main(args: Array[String]): Unit = {
    
    val cache = MemcachedCache()

    case class Person(name: String, address: String)
    val p = Person("ccer", "nsw")

    case class Power(company: String, watts: Double)
    val power = Power("Ausgrid", 100.2)
    //println(p.name)
    //val s = new State("state", "NSW", 31)
    //cache.set[Power]("power", power)
    //cache.set[Person]("person", p)
    //cache.set[String]("test", "gold")
    //cache.get[Person]("person").map(f => println(f.get.address))
    cache.get[Power]("power").map(f => println(f.get.watts))
    //cache.delete("test")

  }

}
