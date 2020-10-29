package exercises.function

import exercises.function.FunctionAnswers._
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.collection.mutable.ListBuffer

class FunctionAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  /////////////////////////////////////////////////////
  // Exercise 1: String API with higher-order functions
  /////////////////////////////////////////////////////

  test("selectDigits") {
    assert(selectDigits("hello4world-80") == "480")
    assert(selectDigits("welcome") == "")
  }

  test("selectDigits length is smaller") {
    forAll { (text: String) =>
      assert(selectDigits(text).length <= text.length)
    }
  }

  test("selectDigits only returns numbers") {
    forAll { (text: String) =>
      selectDigits(text).foreach { char =>
        assert(char.isDigit)
      }
    }
  }

  test("String filter result satisfies predicate") {
    forAll { (text: String, predicate: Char => Boolean) =>
      text.filter(predicate).forall(predicate)
    }
  }

  test("secret") {
    assert(secret("abc123") == "******")
  }

  test("secret is idempotent") {
    forAll { (text: String) =>
      assert(secret(secret(text)) == secret(text))
    }
  }

  test("isValidUsernameCharacter") {
    assert(isValidUsernameCharacter('a'))
    assert(isValidUsernameCharacter('A'))
    assert(isValidUsernameCharacter('1'))
    assert(isValidUsernameCharacter('-'))
    assert(isValidUsernameCharacter('_'))
    assert(!isValidUsernameCharacter('~'))
    assert(!isValidUsernameCharacter('!'))
  }

  test("isValidUsername") {
    assert(isValidUsername("john-doe"))
    assert(!isValidUsername("*john*"))
  }

  test("if a username is case valid, so is its inverse") {
    forAll { (username: String) =>
      assert(isValidUsername(username.reverse) == isValidUsername(username))
    }
  }

  test("if two usernames are valid, then concatenating them form a valid username") {
    forAll { (username1: String, username2: String) =>
      val lhs = isValidUsername(username1 + username2)
      val rhs = isValidUsername(username1) && isValidUsername(username2)
      assert(lhs == rhs)
    }
  }

  ///////////////////////
  // Exercise 2: Point
  ///////////////////////

  test("isPositive") {
    assert(Point(2, 4, 9).isPositive)
    assert(Point(0, 0, 0).isPositive)
    assert(!Point(0, -2, 1).isPositive)
  }

  test("isPositive max 0") {
    forAll { (x: Int, y: Int, z: Int) =>
      assert(Point(x.max(0), y.max(0), z.max(0)).isPositive)
    }
  }

  test("isPositive with positive generator") {
    forAll(Gen.posNum[Int], Gen.posNum[Int], Gen.posNum[Int]) { (x: Int, y: Int, z: Int) =>
      assert(Point(x, y, z).isPositive)
    }
  }

  test("isEven") {
    assert(Point(2, 4, 8).isEven)
    assert(Point(0, -8, -2).isEven)
    assert(!Point(3, -2, 0).isEven)
  }

  test("isEven * 2") {
    forAll { (x: Int, y: Int, z: Int) =>
      assert(Point(x * 2, y * 2, z * 2).isEven)
    }
  }

  test("forAll") {
    assert(Point(1, 1, 1).forAll(_ == 1))
    assert(!Point(1, 2, 5).forAll(_ == 1))
  }

  test("forAll constant") {
    forAll { (x: Int, y: Int, z: Int, constant: Boolean) =>
      assert(Point(x, y, z).forAll(_ => constant) == constant)
    }
  }

  test("forAll consistent with List") {
    forAll { (x: Int, y: Int, z: Int, predicate: Int => Boolean) =>
      assert(Point(x, y, z).forAll(predicate) == List(x, y, z).forall(predicate))
    }
  }

  ////////////////////////////
  // 2. polymorphic functions
  ////////////////////////////

  test("Pair#map") {
    assert(Pair("John", "Doe").map(_.length) == Pair(4, 3))
  }

  test("identity") {
    forAll { (x: Int) =>
      assert(identity(x) == x)
    }
  }

  test("const") {
    forAll { (x: Int, y: String) =>
      assert(const(x)(y) == x)
    }

    assert(List(1, 2, 3).map(const(0)) == List(0, 0, 0))
  }

  test("setOption") {
    assert(setOption(Some(5))("Hello") == Some("Hello"))
    assert(setOption(None)("Hello") == None)
  }

  test("andThen - compose") {
    val isEven = (_: Int) % 2 == 0
    val inc    = (_: Int) + 1
    assert(compose(isEven, inc)(10) == false)
    assert(andThen(inc, isEven)(10) == false)
  }

  test("doubleInc") {
    assert(doubleInc(0) == 1)
    assert(doubleInc(6) == 13)
  }

  test("incDouble") {
    assert(incDouble(0) == 2)
    assert(incDouble(6) == 14)
  }

  ///////////////////////////
  // 3. Recursion & Laziness
  ///////////////////////////

  List(sumList _, sumList2 _, sumList3 _).zipWithIndex.foreach {
    case (f, i) =>
      test(s"sumList $i small") {
        assert(f(List(1, 2, 3, 10)) == 16)
        assert(f(Nil) == 0)
      }
  }

  List(sumList2 _, sumList3 _).zipWithIndex.foreach {
    case (f, i) =>
      test(s"sumList $i large") {
        val xs = 1.to(1000000).toList

        assert(f(xs) == xs.sum)
      }
  }

  List(mkString _, mkString2 _).zipWithIndex.foreach {
    case (f, i) =>
      test(s"mkString $i") {
        forAll { (s: String) =>
          assert(f(s.toList) == s)
        }
      }
  }

  test("multiply") {
    assert(multiply(Nil) == 1)
    assert(multiply(List(0, 2, 4)) == 0)
    assert(multiply(List(1, 2, 4)) == 8)

    forAll { (x: Int, xs: List[Int]) =>
      assert(multiply(x :: xs) == (x * multiply(xs)))
    }

    forAll { (xs: List[Int]) =>
      assert(multiply(xs) == multiply(xs.reverse))
    }
  }

  test("filter") {
    forAll { (xs: List[Int], p: Int => Boolean) =>
      assert(filter(xs)(p) == xs.filter(p))
    }
  }

  List(FunctionAnswers.forAll _, FunctionAnswers.forAll2 _).zipWithIndex.foreach {
    case (f, i) =>
      test(s"forAll $i") {
        assert(f(List(true, true, true)) == true)
        assert(f(List(true, false, true)) == false)
        assert(f(Nil) == true)
      }

      if (i == 0)
        test(s"forAll $i is stack safe") {
          val xs = List.fill(1000000)(true)

          assert(f(xs) == true)
        }
  }

  List(find[Int] _, find2[Int] _).zipWithIndex.foreach {
    case (f, i) =>
      test(s"find $i") {
        val xs = 1.to(100).toList

        assert(f(xs)(_ == 5) == Some(5))
        assert(f(xs)(_ == -1) == None)
      }

      test(s"find $i is lazy") {
        val xs = 1.to(1000000).toList

        val seen = ListBuffer.empty[Int]

        val res = f(xs) { x =>
          seen += x; x > 10
        }

        assert(res == Some(11))
        assert(seen.size == 11)
      }

      if (i == 0)
        test(s"find $i is stack safe") {
          val xs = 1.to(10000000).toList

          assert(f(xs)(_ == 5) == Some(5))
          assert(f(xs)(_ == -1) == None)
        }
  }

  test("headOption") {
    assert(headOption(List(1, 2, 3, 4)) == Some(1))
    assert(headOption(Nil) == None)
    assert(headOption(List.fill(10000000)(1)) == Some(1))
  }

  ////////////////////////
  // 5. Memoization
  ////////////////////////

  test("memoize") {
    def inc(x: Int): Int                         = x + 1
    def circleCircumference(radius: Int): Double = 2 * radius * Math.PI

    forAll { (x: Int) =>
      assert(memoize(inc)(x) == inc(x))
    }
    forAll { (x: Int) =>
      assert(memoize(circleCircumference)(x) == circleCircumference(x))
    }
  }

}
