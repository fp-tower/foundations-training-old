package exercises.errorhandling

import exercises.action.IOAnswers.IO
import exercises.action.IORef
import exercises.errorhandling.NestedErrorAnswers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util.Failure

class NestedErrorAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {
  case class InMemoryDb(users: IORef[Map[UserId, User]], orders: IORef[Map[OrderId, Order]]) extends Db {
    def getUser(userId: NestedErrorAnswers.UserId): IO[Option[NestedErrorAnswers.User]] =
      users.get.map(_.get(userId))

    def getOrder(orderId: OrderId): IO[Option[Order]] =
      orders.get.map(_.get(orderId))

    def upsertOrder(order: Order): IO[Unit] =
      orders.update(_.updated(order.id, order))
  }

  case class InMemoryEmailClient(ref: IORef[List[(Email, String)]]) extends EmailClient {
    def sendEmail(email: Email, body: String): IO[Unit] =
      ref.update(_ :+ (email, body))
  }

  def newInMemoryDb(users: List[User] = Nil, orders: List[Order] = Nil) =
    for {
      usersRef  <- IORef(users.groupMapReduce(_.id)(identity)((u, _) => u))
      ordersRef <- IORef(orders.groupMapReduce(_.id)(identity)((o, _) => o))
    } yield InMemoryDb(usersRef, ordersRef)

  val newInMemoryEmailClient = IORef(List.empty[(Email, String)]).map(InMemoryEmailClient)

  val ec = scala.concurrent.ExecutionContext.global

  test("sendUserEmail") {
    val user1  = User(UserId(10), "John", Some(Email("j@foo.com")))
    val user2  = User(UserId(11), "Eda", None)
    val order1 = Order(OrderId(1), user1.id, "DELIVERED", 123.45)
    val order2 = Order(OrderId(2), user2.id, "DELIVERED", 123.45)
    val order3 = Order(OrderId(3), UserId(-1), "DELIVERED", 123.45)
    (for {
      db          <- newInMemoryDb(users = List(user1, user2), orders = List(order1, order2, order3))
      emailClient <- newInMemoryEmailClient
      userApi = new NotificationApi(db, emailClient)
      _           <- userApi.sendDeliveredMessage(order1.id)
      order2Res   <- userApi.sendDeliveredMessage(order2.id).attempt
      order3Res   <- userApi.sendDeliveredMessage(order3.id).attempt
      order4Res   <- userApi.sendDeliveredMessage(OrderId(-1)).attempt
      expectEmail <- emailClient.ref.get
    } yield {
      assert(expectEmail == List(user1.email.get -> s"Order ${order1.id} delivered"))
      assert(order2Res == Failure(UserWithoutEmail(user2.id)))
      assert(order3Res == Failure(UserNotFound(UserId(-1))))
      assert(order4Res == Failure(OrderNotFound(OrderId(-1))))
    }).unsafeRun()
  }

}
