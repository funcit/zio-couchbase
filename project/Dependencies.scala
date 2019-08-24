import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  val dependencies = Seq(
    "com.couchbase.client" %% "scala-client" % "1.0.0-alpha.6",
    "dev.zio" %% "zio"                       % "1.0.0-RC11-1",
    "com.lihaoyi" %% "upickle"               % "0.7.1" % "optional",
    "io.circe" %% "circe-core"               % "0.10.0" % "optional",
    "com.typesafe.play" %% "play-json"       % "2.6.7" % "optional",
    "org.json4s" %% "json4s-native"          % "3.6.4" % "optional",
    "org.typelevel" %% "jawn-ast"            % "0.14.0" % "optional"
  )
}
