package com.funcit

import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.api.{ExistsResult, MutationResult}
import com.couchbase.client.scala.codec.Conversions
import com.couchbase.client.scala.kv._
import com.funcit.ZImplicits._
import zio.{Fiber, Task, UIO}

import scala.concurrent.Future
import scala.concurrent.duration._

class ZCollection private[funcit] (collection: Task[AsyncCollection]) {

  def get(id: String): Task[Fiber[Throwable, GetResult]] = {
    collection.flatMap(_.get(id).fromFuture).fork
  }

  def insert[T](id: String, content: T)(
    implicit ev: Conversions.Encodable[T]): UIO[Fiber[Throwable, MutationResult]] = {
    collection.flatMap(_.insert(id, content).fromFuture).fork
  }

  def replace[T](id: String, content: T, cas: Long = 0)(
    implicit ev: Conversions.Encodable[T]): UIO[Fiber[Throwable, MutationResult]] = {
    collection.flatMap(_.replace(id, content, cas).fromFuture).fork
  }

  def upsert[T](id: String, content: T)(
    implicit ev: Conversions.Encodable[T]): UIO[Fiber[Throwable, MutationResult]] = {
    collection.flatMap(_.upsert(id, content).fromFuture).fork
  }

  def remove(id: String, cas: Long = 0): UIO[Fiber[Throwable, MutationResult]] = {
    collection.flatMap(_.remove(id, cas).fromFuture).fork
  }

  def mutateIn(id: String, spec: Seq[MutateInSpec], cas: Long = 0): UIO[Fiber[Throwable, MutateInResult]] = {
    collection.flatMap(_.mutateIn(id, spec, cas).fromFuture).fork
  }

  def getAndLock(id: String, lockFor: Duration = 30.seconds): UIO[Fiber[Throwable, GetResult]] = {
    collection.flatMap(_.getAndLock(id, lockFor).fromFuture).fork
  }

  def unlock(id: String, cas: Long): UIO[Fiber[Throwable, Unit]] = {
    collection.flatMap(_.unlock(id, cas).fromFuture).fork
  }

  def getAndTouch(id: String, expiration: Duration): UIO[Fiber[Throwable, GetResult]] = {
    collection.flatMap(_.getAndTouch(id, expiration).fromFuture).fork
  }

  def lookupIn(id: String, spec: Seq[LookupInSpec]): UIO[Fiber[Throwable, LookupInResult]] = {
    collection.flatMap(_.lookupIn(id, spec).fromFuture).fork
  }

  def getAnyReplica(id: String): UIO[Fiber[Throwable, GetResult]] = {
    collection.flatMap(_.getAnyReplica(id).fromFuture).fork
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  def getAllReplicas(id: String): UIO[Fiber[Throwable, Seq[GetResult]]] = {
    (for {
      coll   <- collection
      r      <- coll.getAllReplicas(id).toZIO
      result <- Future.sequence(r).fromFuture
    } yield result).fork
  }

  def exists(id: String): UIO[Fiber[Throwable, ExistsResult]] = {
    collection.flatMap(_.exists(id).fromFuture).fork
  }
}
