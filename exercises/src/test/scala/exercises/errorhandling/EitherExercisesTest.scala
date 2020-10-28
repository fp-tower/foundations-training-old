package exercises.errorhandling

import exercises.errorhandling.EitherExercises.UserEmailError.{EmailNotFound, UserNotFound}
import exercises.errorhandling.EitherExercises.UsernameError.{InvalidCharacters, TooSmall}
import exercises.errorhandling.EitherExercises._
import exercises.errorhandling.OptionExercises
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class EitherExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  ////////////////////////
  // 1. Use cases
  ////////////////////////

  ignore("getUserEmail") {
    import OptionExercises.{Email, User, UserId}
    val userMap = Map(
      UserId(222) -> User(UserId(222), "john", Some(Email("j@x.com"))),
      UserId(123) -> User(UserId(123), "elisa", Some(Email("e@y.com"))),
      UserId(444) -> User(UserId(444), "bob", None)
    )

    assert(getUserEmail(UserId(123), userMap) == Right(Email("e@y.com")))
    assert(getUserEmail(UserId(111), userMap) == Left(UserNotFound(UserId(111))))
    assert(getUserEmail(UserId(444), userMap) == Left(EmailNotFound(UserId(444))))
  }

  ignore("checkout") {
    val item      = Item("xxx", 2, 12.34)
    val baseOrder = Order("123", "Draft", List(item), None, None, None)

    assert(checkout(baseOrder) == Right(baseOrder.copy(status = "Checkout")))
  }

  test("submit") {}

  test("deliver") {}

  //////////////////////////////////
  // 2. Import code with Exception
  //////////////////////////////////

  test("parseUUID") {}

  //////////////////////////////////
  // 3. Advanced API
  //////////////////////////////////

  ignore("validateUsername") {
    validateUsername("foo") == Right(Username("foo"))
    validateUsername("  foo ") == Right(Username("foo"))
    validateUsername("a!bc@£") == Left(InvalidCharacters("!@£".toList))
    validateUsername(" yo") == Left(TooSmall(2))
    validateUsername(" !") == Left(TooSmall(1))
  }

  test("validateUsernameSize") {}

  test("validateUsernameCharacters") {}

  test("validateUser") {}

  test("validateUserAcc") {}

  test("sequenceAcc") {}

}
