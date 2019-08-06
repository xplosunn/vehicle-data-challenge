package com.github.xplosunn.vehicledata.controller

import cats.Id

import scala.concurrent.Future

trait ToFuture[F[_]] {
  def apply[A](f: F[A]): Future[A]
}

object ToFuture {
  def apply[F[_]](implicit tf: ToFuture[F]): ToFuture[F] = tf

  implicit val future = new ToFuture[Future] {
    override def apply[A](f: Future[A]): Future[A] = f
  }

  implicit val id = new ToFuture[Id] {
    override def apply[A](f: Id[A]): Future[A] = Future.successful(f)
  }
}
