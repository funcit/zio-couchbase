package com.funcit

import com.couchbase.client.scala.api.MutationResult
import com.couchbase.client.scala.json.JsonObject
import com.couchbase.client.scala.kv.GetResult
import com.funcit.ZCluster._
import org.scalatest.{FlatSpec, Matchers}
import zio.DefaultRuntime

class ZClusterSpec extends FlatSpec with Matchers with DefaultRuntime {

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

    unsafeRun(result).cas should be > 0L
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
    } yield existsValue.exists

    unsafeRun(result) should be(true)
  }
}
