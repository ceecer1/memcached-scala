package com.kodeasync.memcached.util

import java.io.{BufferedReader, InputStreamReader}

import akka.actor.ActorRef
import com.kodeasync.memcached.handler.CommandHandler.{CommandResponse, DeleteCommand, GetCommand, SetCommand}
import com.kodeasync.memcached.serialize.Codec
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by shishir on 10/30/16.
  * This class implements the cache operations get, set and delete for the memcached cache
  */
class MemcachedCache(actor: ActorRef)(implicit executionContext: ExecutionContext) extends Cache[Array[Byte]]{

  implicit val timeout = Timeout(5 seconds)

  /**
    * This method returns the value of the type V if the key matches
    * @param key The cache key to fetch
    * @param codec The codec to deserialize instance of Array[Byte] into the type V object
    * @tparam V The type of Value to return
    * @return Returns a cached Value
    */
  override def get[V](key: String)(implicit codec: Codec[V, Array[Byte]]): Future[Option[V]] = {
    val getCommand = GetCommand(key)
    val result = (actor ? getCommand).mapTo[CommandResponse]
    result.map { r =>
      val res = new String(r.data, "UTF-8")
      val arr = res.split("\r\n")
      val startByteLength = (arr(0) + "\r\n").getBytes.length
      val endByteLength = (arr(2) + "\r\n").getBytes.length
      val totalLenght = r.data.length
      val dataArray = r.data.slice(startByteLength, totalLenght - endByteLength - 2)

      Some(codec.deserialize(dataArray))
    }
  }

  /**
    * This method represents the set behaviour
    * @param key The cache key to set
    * @param value The cache value to set
    * @param codec The codec to serialize the value of type V
    * @tparam V The type of the value to set
    * @return Returns Boolean value to represent the operation status
    */
  override def set[V](key: String, value: V, expiry: Int = 0)(implicit codec: Codec[V, Array[Byte]]): Future[Unit] = {
    val valueToCache = codec.serialize(value)
    val setCommand = SetCommand(key, expiry, valueToCache)
    val result = (actor ? setCommand).mapTo[CommandResponse]
    result.map { r =>
      val res = new String(r.data, "UTF-8")
      println(res)
    }
  }

  /**
    * This method deletes an existing key and its value from cache
    * @param key The key which is to be removed from the cache
    * @return Returns Boolean value to represent the operation status
    */
  override def delete(key: String) = {
    val deleteCommand = DeleteCommand(key)
    val result = (actor ? deleteCommand).mapTo[CommandResponse]
    result.map { r =>
      val res = new String(r.data, "UTF-8")
      println(res)
    }
  }

}
