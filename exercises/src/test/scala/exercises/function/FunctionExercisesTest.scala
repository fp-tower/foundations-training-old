package exercises.function

import exercises.function.FunctionExercises._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  /////////////////////////////////////////////////////
  // Exercise 1: String API with higher-order functions
  /////////////////////////////////////////////////////

  // replace `ignore` by `test` to enable the test
  ignore("selectDigits examples") {
    assert(selectDigits("hello4world-80") == "480")
    assert(selectDigits("welcome") == "")
  }

  // replace `ignore` by `test` to enable the test
  ignore("selectDigits length is smaller") {
    forAll { (text: String) =>
      assert(selectDigits(text).length <= text.length)
    }
  }

  ///////////////////////
  // Exercise 2: Point
  ///////////////////////

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
