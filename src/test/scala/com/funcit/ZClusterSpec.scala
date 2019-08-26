package com.funcit

import com.couchbase.client.scala.json.JsonObject
import com.funcit.ZCluster._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import zio.DefaultRuntime

class ZClusterSpec extends FlatSpec with Matchers with DefaultRuntime with BeforeAndAfterAll {

  private lazy val connectionString = "localhost"
  private lazy val username = "Administrator"
  private lazy val password = "password"

  "insert, get and remove" should "insert, fetch and delete the document" in {
    val result = for {
      conn        <- connect(connectionString, username, password)
      bkt         <- conn.bucket("test")
      coll        <- bkt.defaultCollection
      docInsFiber <- coll.insert("doc1", JsonObject("hello" -> "world"))
      _           <- docInsFiber.join
      docGetFiber <- coll.get("doc1")
      _           <- docGetFiber.join
      removeFiber <- coll.remove("doc1")
      removeValue <- removeFiber.join
    } yield removeValue

    unsafeRun(result).get.cas should be > 0L
  }

  "exists" should "check if the document exists" in {
    val result = for {
      conn        <- connect(connectionString, username, password)
      bkt         <- conn.bucket("test")
      coll        <- bkt.defaultCollection
      docInsFiber <- coll.insert("doc1", JsonObject("hello" -> "world"))
      _           <- docInsFiber.join
      existsFiber <- coll.exists("doc1")
      existsValue <- existsFiber.join
      removeFiber <- coll.remove("doc1")
      _           <- removeFiber.join
    } yield existsValue

    unsafeRun(result).exists should be(true)
  }

  "upsert" should "update or insert a document if does / does not exist" in {
    val result = for {
      conn        <- connect(connectionString, username, password)
      bkt         <- conn.bucket("test")
      coll        <- bkt.defaultCollection
      docUpsFiber <- coll.upsert("doc2", JsonObject("hello" -> "whole world"))
      upsertValue <- docUpsFiber.join
      removeFiber <- coll.remove("doc2")
      _           <- removeFiber.join
    } yield upsertValue

    unsafeRun(result).cas > 0L
  }
}
