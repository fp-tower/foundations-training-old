package exercises.errorhandling

import java.time.Instant

import exercises.action.IOAnswers.IO
import exercises.action.IORef
import exercises.errorhandling.NestedErrorAnswers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

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

  val orderService = new OrderService {
    def submit(order: Order, now: Instant): Either[OrderError, Order] =
      Right(order.copy(status = "CHECKOUT"))

    def confirm(order: Order, now: Instant): Either[OrderError, Order] =
      Right(order.copy(status = "CONFIRMED"))
  }

  val clock = IO.succeed(Instant.now())

  test("invoice") {
    val user1  = User(UserId(10), "John", Some(Email("j@foo.com")))
    val order1 = Order(OrderId(1), user1.id, "SUBMITTED", 123.45)
    (for {
      db          <- newInMemoryDb(users = List(user1), orders = List(order1))
      emailClient <- newInMemoryEmailClient
      userApi = new Api(orderService, db, emailClient, clock)
      invoice <- userApi.generateInvoice(order1.id)
    } yield {
      assert(invoice.total == order1.total)
    }).unsafeRun()
  }

  test("confirm") {
    val user1  = User(UserId(10), "John", Some(Email("j@foo.com")))
    val order1 = Order(OrderId(1), user1.id, "SUBMITTED", 123.45)
    (for {
      db          <- newInMemoryDb(users = List(user1), orders = List(order1))
      emailClient <- newInMemoryEmailClient
      userApi = new Api(orderService, db, emailClient, clock)
      _           <- userApi.confirm(order1.id)
      order       <- db.getOrder(order1.id)
      expectEmail <- emailClient.ref.get
    } yield {
      assert(order.map(_.status) == Some("CONFIRMED"))
      assert(expectEmail == List(user1.email.get -> s"Order ${order1.id} confirmed"))
    }).unsafeRun()
  }

}
