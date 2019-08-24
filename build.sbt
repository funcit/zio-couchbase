import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.funcit"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "zio-couchbase",
    libraryDependencies ++= dependencies
  )

resolvers += "Couchbase Snapshots" at "http://files.couchbase.com/maven2"