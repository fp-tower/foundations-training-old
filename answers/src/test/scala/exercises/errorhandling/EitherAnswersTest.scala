package exercises.errorhandling

import java.time.{Duration, Instant, LocalDate, ZoneOffset}
import java.util.UUID

import exercises.errorhandling.EitherAnswers.CountryError.InvalidFormat
import exercises.errorhandling.EitherAnswers.UserEmailError.{EmailNotFound, UserNotFound}
import exercises.errorhandling.EitherAnswers.UsernameError.{InvalidCharacters, TooSmall}
import exercises.errorhandling.EitherAnswers._
import exercises.errorhandling.OptionAnswers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class EitherAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  def startOfDay(year: Int, month: Int, day: Int): Instant =
    LocalDate.of(year, month, day).atStartOfDay.toInstant(ZoneOffset.UTC)

  ////////////////////////
  // 1. Use cases
  ////////////////////////

  test("getUserEmail") {
    import OptionAnswers.{Email, User, UserId}
    val userMap = Map(
      UserId(222) -> User(UserId(222), "john", Some(Email("j@x.com"))),
      UserId(123) -> User(UserId(123), "elisa", Some(Email("e@y.com"))),
      UserId(444) -> User(UserId(444), "bob", None)
    )

    assert(getUserEmail(UserId(123), userMap) == Right(Email("e@y.com")))
    assert(getUserEmail(UserId(111), userMap) == Left(UserNotFound(UserId(111))))
    assert(getUserEmail(UserId(444), userMap) == Left(EmailNotFound(UserId(444))))
  }

  test("checkout") {
    val item      = Item("xxx", 2, 12.34)
    val baseOrder = Order("123", "Draft", List(item), None, None, None)

    assert(checkout(baseOrder) == Right(baseOrder.copy(status = "Checkout")))
    assert(checkout(baseOrder.copy(basket = Nil)) == Left(OrderError.EmptyBasket))
    assert(checkout(baseOrder.copy(status = "Delivered")) == Left(OrderError.InvalidStatus("checkout", "Delivered")))
  }

  test("submit") {
    val item      = Item("xxx", 2, 12.34)
    val now       = startOfDay(2019, 6, 12)
    val baseOrder = Order("123", "Checkout", List(item), Some("10 high street"), None, None)

    assert(submit(baseOrder, now) == Right(baseOrder.copy(status = "Submitted", submittedAt = Some(now))))
    assert(submit(baseOrder.copy(deliveryAddress = None), now) == Left(OrderError.MissingDeliveryAddress))
    assert(submit(baseOrder.copy(status = "Draft"), now) == Left(OrderError.InvalidStatus("submit", "Draft")))
  }

  test("deliver") {
    val item        = Item("xxx", 2, 12.34)
    val submittedAt = startOfDay(2019, 6, 12)
    val now         = startOfDay(2019, 6, 15)
    val baseOrder   = Order("123", "Submitted", List(item), Some("10 high street"), Some(submittedAt), None)

    assert(
      deliver(baseOrder, now) == Right(
        (baseOrder.copy(status = "Delivered", deliveredAt = Some(now)), Duration.ofDays(3))
      )
    )
    assert(deliver(baseOrder.copy(submittedAt = None), now) == Left(OrderError.MissingSubmittedTimestamp))
    assert(deliver(baseOrder.copy(status = "Draft"), now) == Left(OrderError.InvalidStatus("deliver", "Draft")))
  }

  //////////////////////////////////
  // 2. Import code with Exception
  //////////////////////////////////

  test("parseUUID") {
    assert(
      parseUUID("123e4567-e89b-12d3-a456-426655440000") == Right(
        UUID.fromString("123e4567-e89b-12d3-a456-426655440000")
      )
    )
    assert(parseUUID("foo").isLeft == true)
  }

  //////////////////////////////////
  // 3. Advanced API
  //////////////////////////////////

  test("validateUsername") {
    assert(validateUsername("foo") == Right(Username("foo")))
    assert(validateUsername("  foo ") == Right(Username("foo")))
    assert(validateUsername("a!bc@£") == Left(InvalidCharacters("!@£".toList)))
    assert(validateUsername(" yo") == Left(TooSmall(2)))
    assert(validateUsername(" !") == Left(TooSmall(1)))
  }

  test("validateUsernameSize") {
    assert(validateUsernameSize("moreThan3Char") == Right(()))
    assert(validateUsernameSize("foo") == Right(()))
    assert(validateUsernameSize("fo") == Left(TooSmall(2)))
  }

  test("validateUsernameCharacters") {
    assert(validateUsernameCharacters("abcABC123-_") == Right(()))
    assert(validateUsernameCharacters("foo!~23}AD") == Left(InvalidCharacters(List('!', '~', '}'))))
  }

  test("validateUser") {
    assert(validateUser("  foo ", "FRA") == Right(User(Username("foo"), Country.France)))
    assert(validateUser("~a", "FRA") == Left(TooSmall(2)))
    assert(validateUser("  foo ", "UK") == Left(InvalidFormat("UK")))
    assert(validateUser("~a", "UK") == Left(TooSmall(2)))
  }

  test("validateUsernameAcc") {
    forAll(
      (username: String) =>
        assert(
          validateUsernameAcc(username) == validateUsernameAccCats(username)
      )
    )
  }

  test("validateUserAcc") {
    assert(validateUserAcc("  foo ", "FRA") == Right(User(Username("foo"), Country.France)))
    assert(validateUserAcc("~a", "FRA") == Left(List(TooSmall(2), InvalidCharacters(List('~')))))
    assert(validateUserAcc("  foo ", "UK") == Left(List(InvalidFormat("UK"))))
    assert(
      validateUserAcc("~a", "UK") == Left(
        List(TooSmall(2), InvalidCharacters(List('~')), CountryError.InvalidFormat("UK"))
      )
    )
  }

  test("sequenceAcc") {
    assert(sequenceAcc(List(Right(1), Right(2), Right(3))) == Right(List(1, 2, 3)))
    assert(sequenceAcc(List(Left(List("e1", "e2")), Right(1), Left(List("e3")))) == Left(List("e1", "e2", "e3")))
  }

}
