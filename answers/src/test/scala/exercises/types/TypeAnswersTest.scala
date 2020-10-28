package exercises.types

import java.time.{Duration, Instant}
import java.util.UUID

import exercises.types.TypeAnswers
import exercises.types.TypeAnswers.OrderStatus._
import exercises.types.TypeAnswers._
import cats.Eq
import cats.data.NonEmptyList
import org.scalacheck.Arbitrary
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class TypeAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  def days(x: Int): Duration = Duration.ofDays(x)

  test("mostRecentBlogs") {
    val now = Instant.now()
    val b1  = BlogPost("123", "foo", now)
    val b2  = BlogPost("222", "bar", now.plus(days(3)))
    val b3  = BlogPost("444", "fuzz", now.plus(days(9)))

    assert(mostRecentBlogs(2)(List(b3, b1, b2)) == List(b1, b2))
  }

  test("invoice") {
    val invoice = Invoice("111",
                          NonEmptyList.of(
                            InvoiceItem("a", 2, 10),
                            InvoiceItem("a", 5, 4)
                          ))
    assert(
      invoice.discountFirstItem(0.5) == Invoice("111",
                                                NonEmptyList.of(
                                                  InvoiceItem("a", 2, 5),
                                                  InvoiceItem("a", 5, 4)
                                                ))
    )
  }

  test("submit") {
    val now     = Instant.now()
    val orderId = OrderId(UUID.randomUUID())
    val itemId  = ItemId(UUID.randomUUID())
    val address = Address(10, "EXC1 7TW")
    val status  = Checkout(NonEmptyList.of(Item(itemId, 1, 2)), Some(address))
    val order   = Order(orderId, now, status)

    assert(
      submit(order, now.plus(days(3))) == Right(
        Order(orderId, now, Submitted(NonEmptyList.of(Item(itemId, 1, 2)), address, now.plus(days(3))))
      )
    )

    val noAddress = order.copy(status = status.copy(deliveryAddress = None))
    assert(submit(noAddress, now.plus(days(3))) == Left(OrderError.MissingDeliveryAddress(noAddress.status)))

    val draftOrder = order.copy(status = Draft(Nil))
    assert(submit(draftOrder, now.plus(days(3))) == Left(OrderError.InvalidStatus(draftOrder.status)))
  }

  test("deliver") {
    val now     = Instant.now()
    val orderId = OrderId(UUID.randomUUID())
    val itemId  = ItemId(UUID.randomUUID())
    val address = Address(10, "EXC1 7TW")
    val order   = Order(orderId, now, Submitted(NonEmptyList.of(Item(itemId, 1, 2)), address, now.plus(days(3))))

    assert(
      deliver(order, now.plus(days(4))) == Right(
        Order(orderId,
              now,
              Delivered(NonEmptyList.of(Item(itemId, 1, 2)), address, now.plus(days(3)), now.plus(days(4))))
      )
    )

    val draftOrder = order.copy(status = Draft(Nil))
    assert(deliver(draftOrder, now.plus(days(4))) == Left(OrderError.InvalidStatus(draftOrder.status)))
  }

  test("cancel") {
    val now          = Instant.now()
    def days(x: Int) = Duration.ofDays(x)
    val orderId      = OrderId(UUID.randomUUID())
    val itemId       = ItemId(UUID.randomUUID())
    val address      = Address(10, "EXC1 7TW")
    val submitted    = Submitted(NonEmptyList.of(Item(itemId, 1, 2)), address, now.plus(days(3)))
    val order        = Order(orderId, now, submitted)

    assert(
      TypeAnswers.cancel(order, now.plus(days(4))) == Right(
        Order(orderId, now, Cancelled(Right(submitted), now.plus(days(4))))
      )
    )

    val draftOrder = order.copy(status = Draft(Nil))
    assert(TypeAnswers.cancel(draftOrder, now.plus(days(4))) == Left(OrderError.InvalidStatus(draftOrder.status)))
  }

}
