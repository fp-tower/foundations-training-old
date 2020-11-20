package exercises.errorhandling

import java.time.Instant

import exercises.action.IOAnswers.IO

import scala.util.control.NoStackTrace

object NestedErrorAnswers {

  case class User(id: UserId, name: String, email: Option[Email])
  case class Order(id: OrderId, client: UserId, status: String, total: Double)
  case class Invoice(orderId: OrderId, client: User, total: Double)

  case class OrderId(value: Long)
  case class UserId(value: Long)
  case class Email(value: String)

  sealed class CommonError(message: String) extends Exception(message) with NoStackTrace

  case class OrderNotFound(id: OrderId)   extends CommonError(s"Order $id not found")
  case class UserNotFound(id: UserId)     extends CommonError(s"User $id not found")
  case class UserWithoutEmail(id: UserId) extends CommonError(s"User $id does not have an email")

  sealed class OrderError(message: String) extends CommonError(message)

  trait Db {
    def getUser(userId: UserId): IO[Option[User]]
    def getOrder(orderId: OrderId): IO[Option[Order]]
    def upsertOrder(order: Order): IO[Unit]
  }

  trait OrderService {
    def submit(order: Order, now: Instant): Either[OrderError, Order]
    def confirm(order: Order, now: Instant): Either[OrderError, Order]
  }

  trait EmailClient {
    def sendEmail(email: Email, body: String): IO[Unit]
  }

  class Api(orderService: OrderService, db: Db, emailClient: EmailClient, clock: IO[Instant]) {
    def generateInvoice(orderId: OrderId): IO[Invoice] =
      for {
        order <- db.getOrder(orderId).orFail(OrderNotFound(orderId))
        user  <- db.getUser(order.client).orFail(UserNotFound(order.client))
      } yield Invoice(order.id, user, order.total)

    def generateInvoiceV2(order: Order): IO[Invoice] =
      for {
        user <- db.getUser(order.client).orFail(UserNotFound(order.client))
      } yield Invoice(order.id, user, order.total)

    def confirm(orderId: OrderId): IO[Unit] =
      for {
        order    <- db.getOrder(orderId).orFail(OrderNotFound(orderId))
        now      <- clock
        newOrder <- orderService.confirm(order, now).orFail
        _        <- db.upsertOrder(newOrder)
        user     <- db.getUser(order.client).orFail(UserNotFound(order.client))
        _        <- sendUserEmail(user, s"Order $orderId confirmed")
      } yield ()

    def sendUserEmail(user: User, body: String): IO[Unit] =
      for {
        email <- IO.fromEither(user.email.toRight(UserWithoutEmail(user.id)))
        _ <- emailClient
          .sendEmail(email, body)
          .retry(3)
          .handleErrorWith(
            _ => IO(println(s"Fail to send email to ${user.id} after 3 retries"))
          )
      } yield ()
  }

  implicit class IOOptionOps[A](self: IO[Option[A]]) {
    def orFail(e: Throwable): IO[A] =
      self.flatMap {
        case None    => IO.fail(e)
        case Some(a) => IO.succeed(a)
      }
  }

  implicit class IOEitherOps[E <: Throwable, A](self: IO[Either[E, A]]) {
    def orFail: IO[A] =
      self.flatMap {
        case Left(e)  => IO.fail(e)
        case Right(a) => IO.succeed(a)
      }
  }

  implicit class EitherOps[E <: Throwable, A](self: Either[E, A]) {
    def orFail: IO[A] = IO.fromEither(self)
  }

}
