package com.funcit

import zio.{Task, ZIO}

import scala.concurrent.Future
import scala.util.Try

object ZImplicits {

  implicit class ConvertToZIO[A](a: A) {
    def toZIO: Task[A] = ZIO.apply(a)
  }

  implicit class ConvertFromTry[A](a: Try[A]) {
    def fromTry: Task[A] = ZIO.fromTry(a)
  }

  implicit class ConvertFromFuture[A](a: Future[A]) {
    def fromFuture: Task[A] = ZIO.fromFuture(implicit ec => a)
  }
}
