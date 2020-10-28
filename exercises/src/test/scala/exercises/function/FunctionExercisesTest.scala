package exercises.function

import exercises.function.FunctionExercises._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  ////////////////////////////
  // 1. first class functions
  ////////////////////////////

  ignore("isEven") {
    assert(isEven(2) == true)
    assert(isEven(3) == false)

    assert(isEven(-2) == true)
    assert(isEven(-3) == false)
  }

  ignore("isEvenVal") {
    forAll { (x: Int) =>
      assert(isEvenVal(x) == isEven(x))
    }
  }

  test("isEvenDefToVal") {}

  test("move") {}

  ////////////////////////////
  // 2. polymorphic functions
  ////////////////////////////

  ignore("identity") {
    assert(identity(3) == 3)
    assert(identity("foo") == "foo")
  }

  ignore("const") {
    assert(const("foo")(5) == "foo")
    assert(const(5)("foo") == 5)
    assert(List(1, 2, 3).map(const(0)) == List(0, 0, 0))
  }

  ///////////////////////////
  // 3. Recursion & Laziness
  ///////////////////////////

  ignore("sumList small") {
    assert(sumList(List(1, 2, 3, 10)) == 16)
    assert(sumList(Nil) == 0)
  }

  ignore("sumList large") {
    val xs = 1.to(1000000).toList

    assert(sumList(xs) == xs.sum)
  }

}
