package exercises.types

import java.time.Instant
import java.util.UUID

import exercises.action.IOAnswers.IO
import exercises.types.Comparison._
import cats.data.NonEmptyList
import eu.timepit.refined.types.numeric.PosInt

object TypeAnswers {

  ////////////////////////
  // 1. Misused answers.types
  ////////////////////////

  def compareChar(x: Char, y: Char): Comparison =
    if (x < y) LessThan
    else if (x > y) GreaterThan
    else EqualTo

  case class BlogPost(id: String, title: String, createAt: Instant)

  def mostRecentBlogs(n: Int)(blogs: List[BlogPost]): List[BlogPost] =
    blogs.sortBy(_.createAt).take(n)

  def mostRecentBlogs_V2(n: PosInt)(blogs: List[BlogPost]): List[BlogPost] =
    blogs.sortBy(_.createAt).take(n.value)

  case class User(name: String, address: Option[UserAddress])

  case class UserAddress(streetNumber: Int, streetName: String, postCode: String) {
    def fullAddress: String = s"$streetNumber $streetName $postCode"
  }

  case class InvoiceItem(id: String, quantity: Int, price: Double) {
    def discount(discountPercent: Double): InvoiceItem =
      copy(price = price * (1 - discountPercent))
  }
  case class Invoice(id: String, items: NonEmptyList[InvoiceItem]) {
    def discountFirstItem(discountPercent: Double): Invoice = {
      val newItem = items.head.discount(discountPercent)
      copy(items = items.copy(head = newItem))
    }
  }

  def genTicket(title: String): IO[Ticket] =
    for {
      ticketId  <- genTicketId
      createdAt <- readNow
    } yield createTicket(ticketId, title, createdAt)

  def createTicket(ticketId: TicketId, title: String, createdAt: Instant): Ticket =
    Ticket(
      id = ticketId,
      title = title,
      storyPoints = 0,
      createdAt = createdAt
    )

  def genTicketId: IO[TicketId] = IO.effect(TicketId(UUID.randomUUID()))
  def readNow: IO[Instant]      = IO.effect(Instant.now())

  case class TicketId(value: UUID)
  case class Ticket(id: TicketId, title: String, storyPoints: Int, createdAt: Instant)

  ////////////////////////
  // 2. Data Encoding
  ////////////////////////

  case class OrderId(value: UUID)
  case class Order(id: OrderId, createdAt: Instant, status: OrderStatus)

  sealed trait OrderStatus
  object OrderStatus {
    case class Draft(basket: List[Item])                                                             extends OrderStatus
    case class Checkout(basket: NonEmptyList[Item], deliveryAddress: Option[Address])                extends OrderStatus
    case class Submitted(basket: NonEmptyList[Item], deliveryAddress: Address, submittedAt: Instant) extends OrderStatus
    case class Delivered(basket: NonEmptyList[Item],
                         deliveryAddress: Address,
                         submittedAt: Instant,
                         deliveredAt: Instant)
        extends OrderStatus
    case class Cancelled(previousState: Either[Checkout, Submitted], cancelledAt: Instant) extends OrderStatus
  }

  case class ItemId(value: UUID)
  case class Item(id: ItemId, quantity: Long, price: BigDecimal)

  case class Address(streetNumber: Int, postCode: String)

  import exercises.types.TypeAnswers.OrderStatus._

  def submit(order: Order, now: Instant): Either[OrderError, Order] =
    order.status match {
      case x: Checkout =>
        x.deliveryAddress match {
          case None => Left(OrderError.MissingDeliveryAddress(x))
          case Some(address) =>
            val newStatus = Submitted(x.basket, address, submittedAt = now)
            Right(order.copy(status = newStatus))
        }
      case _: Draft | _: Submitted | _: Delivered | _: Cancelled =>
        Left(OrderError.InvalidStatus(order.status))
    }

  def deliver(order: Order, now: Instant): Either[OrderError.InvalidStatus, Order] =
    order.status match {
      case x: Submitted =>
        val newStatus = Delivered(x.basket, x.deliveryAddress, x.submittedAt, deliveredAt = now)
        Right(order.copy(status = newStatus))
      case _: Draft | _: Checkout | _: Delivered | _: Cancelled =>
        Left(OrderError.InvalidStatus(order.status))
    }

  def cancel(order: Order, now: Instant): Either[OrderError.InvalidStatus, Order] =
    order.status match {
      case x: Checkout =>
        val newStatus = Cancelled(Left(x), cancelledAt = now)
        Right(order.copy(status = newStatus))
      case x: Submitted =>
        val newStatus = Cancelled(Right(x), cancelledAt = now)
        Right(order.copy(status = newStatus))
      case _: Draft | _: Delivered | _: Cancelled =>
        Left(OrderError.InvalidStatus(order.status))
    }

  sealed trait OrderError
  object OrderError {
    case class MissingDeliveryAddress(status: OrderStatus) extends OrderError
    case class InvalidStatus(status: OrderStatus)          extends OrderError
  }

  case class Order_V2[A](id: OrderId, createdAt: Instant, value: A)

  def submit_V2(order: Order_V2[Checkout], deliveryAddress: Address, now: Instant): Order_V2[Submitted] = {
    val newStatus = Submitted(order.value.basket, deliveryAddress, submittedAt = now)
    order.copy(value = newStatus)
  }

  def deliver_V2(order: Order_V2[Submitted], now: Instant): Order_V2[Delivered] = {
    val Submitted(basket, deliveryAddress, submittedAt) = order.value
    val newStatus                                       = Delivered(basket, deliveryAddress, submittedAt, deliveredAt = now)
    order.copy(value = newStatus)
  }

  def cancelledCheckout(order: Order_V2[Checkout], now: Instant): Order_V2[Cancelled] = {
    val newStatus = Cancelled(Left(order.value), cancelledAt = now)
    order.copy(value = newStatus)
  }

  def cancelledSubmitted(order: Order_V2[Submitted], now: Instant): Order_V2[Cancelled] = {
    val newStatus = Cancelled(Right(order.value), cancelledAt = now)
    order.copy(value = newStatus)
  }

}
