name := "memcached_scala"

version := "1.2"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion = "2.4.12"
  val scalaTestVersion = "3.0.0"

  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "ch.qos.logback"    %  "logback-classic"  % "1.0.13",
    "org.mockito"       % "mockito-all" % "1.10.19" % "test",
    "org.scalatest"     %% "scalatest" % scalaTestVersion   % "test"

  )
}