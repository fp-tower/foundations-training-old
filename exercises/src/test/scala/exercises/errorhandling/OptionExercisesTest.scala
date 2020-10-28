package exercises.errorhandling

import exercises.errorhandling.OptionExercises._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class OptionExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  ignore("getUserEmail") {
    val userMap = Map(
      UserId(222) -> User(UserId(222), "john", Some(Email("j@x.com"))),
      UserId(123) -> User(UserId(123), "elisa", Some(Email("e@y.com"))),
      UserId(444) -> User(UserId(444), "bob", None)
    )

    assert(getUserEmail(UserId(123), userMap) == Some(Email("e@y.com")))
    assert(getUserEmail(UserId(444), userMap) == None)
    assert(getUserEmail(UserId(111), userMap) == None)
  }

  test("optSingleAccountId") {}

  test("optEditor") {}

  ignore("parseShape") {
    assert(parseShape("C 5") == InvOption.Some(Shape.Circle(5)))
  }

  test("filterDigits") {}

  test("checkAllDigits") {}

}
