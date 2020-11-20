package exercises.errorhandling

import java.time.Instant

import exercises.action.IOExercises.IO

object NestedErrorExercises {

  case class User(id: UserId, name: String, email: Option[Email])
  case class Order(id: OrderId, client: UserId, status: String, total: Double)
  case class Invoice(orderId: OrderId, client: User, total: Double)

  case class OrderId(value: Long)
  case class UserId(value: Long)
  case class Email(value: String)

  trait Db {
    def getUser(userId: UserId): IO[Option[User]]
    def getOrder(orderId: OrderId): IO[Option[Order]]
    def upsertOrder(order: Order): IO[Unit]
  }

  class Api(db: Db) {
    // 1. Implement `generateInvoice` which:
    // * fetch an order from the DB.
    // * fetch the user who placed the order from the DB.
    // * Produce an invoice by combining Order and User.
    //
    // Which return type would you use? Why?
    def generateInvoice(orderId: OrderId) = ???

    // 2. Implement `confirm` which:
    // * fetch an order from the DB.
    // * update the Order using `OrderService#confirm`.
    // * save the updated order to the DB.
    // * send an email using `EmailClient` to the user who placed the order
    //   saying "Order ORDER_ID confirmed".
    //   If the email client is not responding retry at most 3 times, if it still fails, log the error.
    // Note:
    def confirm(orderId: OrderId): IO[Unit] =
      ???
  }

  trait EmailClient {
    def sendEmail(email: Email, body: String): IO[Unit]
  }

  trait OrderService {
    def submit(order: Order, now: Instant): Either[OrderError, Order]
    def confirm(order: Order, now: Instant): Either[OrderError, Order]
  }

  sealed trait OrderError
}
