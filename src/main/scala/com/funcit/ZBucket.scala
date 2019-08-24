package com.funcit

import com.couchbase.client.scala._
import com.couchbase.client.scala.view.{ViewOptions, ViewResult}
import com.funcit.ZImplicits._
import zio.Task

class ZBucket private[funcit] (bucket: Task[AsyncBucket]) {

  private val zioCollection = new ZCollection(bucket.flatMap(_.defaultCollection.fromFuture))

  def name: Task[String] = bucket.flatMap(_.name.toZIO)

  def collection(collection: String): Task[AsyncCollection] = {
    bucket.flatMap(_.collection(collection).fromFuture)
  }

  def defaultCollection: Task[ZCollection] = {
    zioCollection.toZIO
  }

  def scope(name: String): Task[AsyncScope] = {
    bucket.flatMap(_.scope(name).fromFuture)
  }

  def defaultScope: Task[AsyncScope] = bucket.flatMap(_.defaultScope.fromFuture)

  def viewQuery(designDoc: String, viewName: String, options: ViewOptions = ViewOptions()): Task[ViewResult] = {
    bucket.flatMap(_.viewQuery(designDoc, viewName, options).fromFuture)
  }
}
