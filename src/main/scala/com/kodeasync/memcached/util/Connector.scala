package com.kodeasync.memcached.util

import com.typesafe.config.ConfigFactory

/**
  * Created by shishir on 10/30/16.
  * A connector object to read memcached configuration
  */
object Connector {

  val config = ConfigFactory.load()
  val host = config.getString("memcached.host")
  val port = config.getInt("memcached.port")

  val address = MemcachedConfiguration(host, port)
}
