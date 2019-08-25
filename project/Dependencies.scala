import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {
  lazy val couchbaseScalaClient = "1.0.0-alpha.6"
  lazy val upickleVersion = "0.7.1"
  lazy val playJsonVersion = "2.6.7"
  lazy val zioVersion = "1.0.0-RC11-1"
  lazy val circeCoreVersion = "0.10.0"
  lazy val json4sVersion = "3.6.4"
  lazy val jawnAstVersion = "0.14.0"
  lazy val scalaTestVersion = "3.0.5"

  val dependencies = Seq(
    "com.couchbase.client"  %% "scala-client"   % couchbaseScalaClient,
    "com.lihaoyi"           %% "upickle"        % upickleVersion        % "optional",
    "com.typesafe.play"     %% "play-json"      % playJsonVersion       % "optional",
    "dev.zio"               %% "zio"            % zioVersion,
    "io.circe"              %% "circe-core"     % circeCoreVersion      % "optional",
    "org.json4s"            %% "json4s-native"  % json4sVersion         % "optional",
    "org.typelevel"         %% "jawn-ast"       % jawnAstVersion        % "optional",
    "org.scalatest"         %% "scalatest"      % scalaTestVersion
  )
}
