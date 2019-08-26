package com.funcit

import cats.implicits._
import com.couchbase.client.core.error.{KeyExistsException, KeyNotFoundException}
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.api.{ExistsResult, MutationResult}
import com.couchbase.client.scala.codec.Conversions
import com.couchbase.client.scala.kv._
import com.funcit.ZImplicits._
import zio._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ZCollection private[funcit] (collection: Task[AsyncCollection]) {

  def get(id: String): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection
      .flatMap(_.get(id).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def insert[T](id: String, content: T)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection
      .flatMap(_.insert(id, content).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyExistsException => ZIO.succeed(None)
      }
      .fork
  }

  def replace[T](id: String, content: T, cas: Long = 0)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection
      .flatMap(_.replace(id, content, cas).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def upsert[T](id: String, content: T)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, MutationResult]] = {
    collection.flatMap(_.upsert(id, content).fromFuture).fork
  }

  def remove(id: String, cas: Long = 0): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection
      .flatMap(_.remove(id, cas).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def mutateIn(id: String, spec: Seq[MutateInSpec], cas: Long = 0): Task[Fiber[Throwable, MutateInResult]] = {
    collection.flatMap(_.mutateIn(id, spec, cas).fromFuture).fork
  }

  def getAndLock(id: String, lockFor: Duration = 30.seconds): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection
      .flatMap(_.getAndLock(id, lockFor).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def unlock(id: String, cas: Long): Task[Fiber[Throwable, Unit]] = {
    collection.flatMap(_.unlock(id, cas).fromFuture).fork
  }

  def getAndTouch(id: String, expiration: Duration): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection
      .flatMap(_.getAndTouch(id, expiration).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def lookupIn(id: String, spec: Seq[LookupInSpec]): Task[Fiber[Throwable, Option[LookupInResult]]] = {
    collection
      .flatMap(_.lookupIn(id, spec).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def getAnyReplica(id: String): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection
      .flatMap(_.getAnyReplica(id).fromFuture)
      .flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def getAllReplicas(id: String)(implicit ec: ExecutionContext): Task[Fiber[Throwable, Seq[GetResult]]] = {
    (for {
      coll <- collection
      r <- coll.getAllReplicas(id).toZIO.catchSome {
        case _: KeyNotFoundException => ZIO.succeed(Seq.empty)
      }
      result <- Future.sequence(r).fromFuture
    } yield result).fork
  }

  def exists(id: String): Task[Fiber[Throwable, ExistsResult]] = {
    collection.flatMap(_.exists(id).fromFuture).fork
  }
}
