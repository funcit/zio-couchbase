package com.funcit

import cats.implicits._
import com.couchbase.client.core.error.{KeyExistsException, KeyNotFoundException}
import com.couchbase.client.core.retry.RetryStrategy
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.api.{ExistsResult, MutationResult}
import com.couchbase.client.scala.codec.Conversions
import com.couchbase.client.scala.durability.Durability
import com.couchbase.client.scala.durability.Durability.Disabled
import com.couchbase.client.scala.kv._
import com.funcit.ZImplicits._
import zio._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ZCollection private[funcit] (collection: Task[AsyncCollection]) {

  def get(
    id: String,
    withExpiration: Boolean = false,
    project: Seq[String] = Seq.empty,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.get(id, withExpiration, project, t, r).fromFuture
        case (Some(t), None) => c.get(id, withExpiration, project, t).fromFuture
        case (None, Some(r)) => c.get(id, withExpiration, project, retryStrategy = r).fromFuture
        case (None, None) => c.get(id, withExpiration, project).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def insert[T](
    id: String,
    content: T,
    durability: Durability = Disabled,
    expiration: Duration = 0.seconds,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.insert(id, content, durability, expiration, t, r).fromFuture
        case (Some(t), None) => c.insert(id, content, durability, expiration, t).fromFuture
        case (None, Some(r)) => c.insert(id, content, durability, expiration, retryStrategy = r).fromFuture
        case (None, None) => c.insert(id, content, durability, expiration).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyExistsException => ZIO.succeed(None)
      }
      .fork
  }

  def replace[T](
    id: String,
    content: T,
    cas: Long = 0,
    durability: Durability = Disabled,
    expiration: Duration = 0.seconds,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.replace(id, content, cas, durability, expiration, t, r).fromFuture
        case (Some(t), None) => c.replace(id, content, cas, durability, expiration, t).fromFuture
        case (None, Some(r)) => c.replace(id, content, cas, durability, expiration, retryStrategy = r).fromFuture
        case (None, None) => c.replace(id, content, cas, durability, expiration).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def upsert[T](
    id: String,
    content: T,
    durability: Durability = Disabled,
    expiration: Duration = 0.seconds,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None)(
    implicit ev: Conversions.Encodable[T]): Task[Fiber[Throwable, MutationResult]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.upsert(id, content, durability, expiration, t, r).fromFuture
        case (Some(t), None) => c.upsert(id, content, durability, expiration, t).fromFuture
        case (None, Some(r)) => c.upsert(id, content, durability, expiration, retryStrategy = r).fromFuture
        case (None, None) => c.upsert(id, content, durability, expiration).fromFuture
      }
    }.fork
  }

  def remove(
    id: String,
    cas: Long = 0,
    durability: Durability = Disabled,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[MutationResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.remove(id, cas, durability, t, r).fromFuture
        case (Some(t), None) => c.remove(id, cas, durability, t).fromFuture
        case (None, Some(r)) => c.remove(id, cas, durability, retryStrategy = r).fromFuture
        case (None, None) => c.remove(id, cas, durability).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def mutateIn(
    id: String,
    spec: Seq[MutateInSpec],
    cas: Long = 0,
    document: DocumentCreation = DocumentCreation.DoNothing,
    durability: Durability = Disabled,
    expiration: Duration = 0.seconds,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, MutateInResult]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.mutateIn(id, spec, cas, document, durability, expiration, t, r).fromFuture
        case (Some(t), None) => c.mutateIn(id, spec, cas, document, durability, expiration, t).fromFuture
        case (None, Some(r)) =>
          c.mutateIn(id, spec, cas, document, durability, expiration, retryStrategy = r).fromFuture
        case (None, None) => c.mutateIn(id, spec, cas, document, durability, expiration).fromFuture
      }
    }.fork
  }

  def getAndLock(
    id: String,
    lockFor: Duration = 30.seconds,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.getAndLock(id, lockFor, t, r).fromFuture
        case (Some(t), None) => c.getAndLock(id, lockFor, t).fromFuture
        case (None, Some(r)) => c.getAndLock(id, lockFor, retryStrategy = r).fromFuture
        case (None, None) => c.getAndLock(id, lockFor).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def unlock(
    id: String,
    cas: Long,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Unit]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.unlock(id, cas, t, r).fromFuture
        case (Some(t), None) => c.unlock(id, cas, t).fromFuture
        case (None, Some(r)) => c.unlock(id, cas, retryStrategy = r).fromFuture
        case (None, None) => c.unlock(id, cas).fromFuture
      }
    }.fork
  }

  def getAndTouch(
    id: String,
    expiration: Duration,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.getAndTouch(id, expiration, t, r).fromFuture
        case (Some(t), None) => c.getAndTouch(id, expiration, t).fromFuture
        case (None, Some(r)) => c.getAndTouch(id, expiration, retryStrategy = r).fromFuture
        case (None, None) => c.getAndTouch(id, expiration).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def lookupIn(
    id: String,
    spec: Seq[LookupInSpec],
    withExpiration: Boolean = false,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[LookupInResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.lookupIn(id, spec, withExpiration, t, r).fromFuture
        case (Some(t), None) => c.lookupIn(id, spec, withExpiration, t).fromFuture
        case (None, Some(r)) => c.lookupIn(id, spec, withExpiration, retryStrategy = r).fromFuture
        case (None, None) => c.lookupIn(id, spec, withExpiration).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def getAnyReplica(
    id: String,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, Option[GetResult]]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.getAnyReplica(id, t, r).fromFuture
        case (Some(t), None) => c.getAnyReplica(id, t).fromFuture
        case (None, Some(r)) => c.getAnyReplica(id, retryStrategy = r).fromFuture
        case (None, None) => c.getAnyReplica(id).fromFuture
      }
    }.flatMap(_.some.toZIO)
      .catchSome {
        case _: KeyNotFoundException => ZIO.succeed(None)
      }
      .fork
  }

  def getAllReplicas(id: String, timeout: Option[Duration] = None, retryStrategy: Option[RetryStrategy] = None)(
    implicit ec: ExecutionContext): Task[Fiber[Throwable, Seq[GetResult]]] = {
    (for {
      coll <- collection
      r <- (timeout, retryStrategy) match {
        case (Some(t), Some(r)) =>
          coll.getAllReplicas(id, t, r).toZIO.catchSome {
            case _: KeyNotFoundException => ZIO.succeed(Seq.empty)
          }
        case (Some(t), None) =>
          coll.getAllReplicas(id, t).toZIO.catchSome {
            case _: KeyNotFoundException => ZIO.succeed(Seq.empty)
          }
        case (None, Some(r)) =>
          coll.getAllReplicas(id, retryStrategy = r).toZIO.catchSome {
            case _: KeyNotFoundException => ZIO.succeed(Seq.empty)
          }
        case (None, None) =>
          coll.getAllReplicas(id).toZIO.catchSome {
            case _: KeyNotFoundException => ZIO.succeed(Seq.empty)
          }
      }
      result <- Future.sequence(r).fromFuture
    } yield result).fork
  }

  def exists(
    id: String,
    timeout: Option[Duration] = None,
    retryStrategy: Option[RetryStrategy] = None): Task[Fiber[Throwable, ExistsResult]] = {
    collection.flatMap { c =>
      (timeout, retryStrategy) match {
        case (Some(t), Some(r)) => c.exists(id, t, r).fromFuture
        case (Some(t), None) => c.exists(id, t).fromFuture
        case (None, Some(r)) => c.exists(id, retryStrategy = r).fromFuture
        case (None, None) => c.exists(id).fromFuture
      }
    }.fork
  }
}
