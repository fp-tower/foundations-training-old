package exercises.errorhandling

import exercises.action.IOExercises.IO

object NestedErrorExercises {

  // 1. Implement `NotificationApi#sendDeliveredMessage` which:
  // * fetch from DB an order
  // * fetch from DB user who placed the order
  // * send email to this user saying "Order ORDER_ID delivered"
  // If the email client is not responding retry at most 3 times
  // If the email client fail, log the last error but do not fail the action
  //
  // Which return type would you use? Why?

  case class User(id: UserId, name: String, email: Option[Email])
  case class Order(id: OrderId, client: UserId, status: String, total: Double)

  case class OrderId(value: Long)
  case class UserId(value: Long)
  case class Email(value: String)

  trait Db {
    def getUser(userId: UserId): IO[Option[User]]
    def getOrder(orderId: OrderId): IO[Option[Order]]
  }

  trait EmailClient {
    def sendEmail(email: Email, body: String): IO[Unit]
  }

  class NotificationApi(db: Db, emailClient: EmailClient) {
    def sendDeliveredMessage(orderId: OrderId) =
      ???
  }

}
