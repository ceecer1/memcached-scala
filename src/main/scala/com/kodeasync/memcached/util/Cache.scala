package com.kodeasync.memcached.util

import com.kodeasync.memcached.serialize.Codec

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shishir on 10/30/16.
  */
trait Cache[T] {

  def get[V](k: String)(implicit codec: Codec[V, T]): Future[Option[V]]
  def set[V](k: String, v: V, expiry: Int)(implicit codec: Codec[V, T]): Future[Unit]
  def delete(k: String): Future[Unit]

}
