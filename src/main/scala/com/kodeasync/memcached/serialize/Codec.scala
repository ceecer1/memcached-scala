package com.kodeasync.memcached.serialize

import scala.annotation.implicitNotFound

/**
  * Created by shishir on 10/30/16.
  */
@implicitNotFound("Could not find any Codec implementation for type ${T} and ${R}")
trait Codec[T, R] {

  def serialize(value: T): R
  def deserialize(data: R): T

}

object Codec extends BaseCodecs with GenericSerializationCodec



