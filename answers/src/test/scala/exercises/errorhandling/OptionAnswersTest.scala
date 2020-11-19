package exercises.errorhandling

import exercises.errorhandling.OptionAnswers.Role._
import exercises.errorhandling.OptionAnswers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class OptionAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  test("getUserEmail") {
    val userMap = Map(
      UserId(222) -> User(UserId(222), "john", Some(Email("j@x.com"))),
      UserId(123) -> User(UserId(123), "elisa", Some(Email("e@y.com"))),
      UserId(444) -> User(UserId(444), "bob", None)
    )

    assert(getUserEmail(UserId(123), userMap) == Some(Email("e@y.com")))
    assert(getUserEmail(UserId(444), userMap) == None)
    assert(getUserEmail(UserId(111), userMap) == None)
  }

  test("optSingleAccountId") {
    assert(Editor(AccountId(123), "Comic Sans").optSingleAccountId == Some(AccountId(123)))
    assert(Reader(AccountId(123), premiumUser = true).optSingleAccountId == Some(AccountId(123)))
    assert(Admin.optSingleAccountId == None)
  }

  test("optEditor") {
    val editor = Editor(AccountId(123), "Comic Sans")
    assert(editor.optEditor == Some(editor))
    assert(Reader(AccountId(123), premiumUser = true).optEditor == None)
    assert(Admin.optEditor == None)
  }

  test("parseShape") {
    assert(parseShape("C 5") == InvOption.Some(Shape.Circle(5)))
    assert(parseShape("R 2 5") == InvOption.Some(Shape.Rectangle(2, 5)))
    assert(parseShape("R 2") == InvOption.None())
    assert(parseShape("C 2 3") == InvOption.None())
    assert(parseShape("W 2 5") == InvOption.None())
  }

  test("filterDigits") {
    assert(filterDigits("a1bc4".toList) == List(1, 4))
  }

  test("checkAllDigits") {
    assert(checkAllDigits("1234".toList) == Some(List(1, 2, 3, 4)))
    assert(checkAllDigits("a1bc4".toList) == None)
  }

}
