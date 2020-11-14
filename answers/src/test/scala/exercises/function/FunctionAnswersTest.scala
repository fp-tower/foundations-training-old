package exercises.function

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import exercises.function.FunctionAnswers._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.collection.mutable.ListBuffer
import scala.util.Try

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
  // Exercise 3: JsonDecoder
  ////////////////////////////

  test("JsonDecoder Int") {
    assert(intDecoder.decode("1234") == 1234)
    assert(intDecoder.decode("-1") == -1)

    assert(Try(intDecoder.decode("hello")).isFailure)
    assert(Try(intDecoder.decode("1111111111111111")).isFailure)
  }

  test("JsonDecoder Int round-trip") {
    forAll { (id: Int) =>
      assert(intDecoder.decode(id.toString) == id)
    }
  }

  test("JsonDecoder UserId") {
    assert(userIdDecoder.decode("1234") == UserId(1234))
    assert(userIdDecoder.decode("-1") == UserId(-1))

    assert(Try(userIdDecoder.decode("hello")).isFailure)
    assert(Try(userIdDecoder.decode("1111111111111111")).isFailure)
  }

  test("JsonDecoder UserId round-trip") {
    forAll { (id: Int) =>
      assert(userIdDecoder.decode(id.toString) == UserId(id))
    }
  }

  test("JsonDecoder LocalDate") {
    assert(localDateDecoder.decode("\"2020-03-26\"") == LocalDate.of(2020, 3, 26))
    assert(Try(localDateDecoder.decode("2020-03-26")).isFailure)
    assert(Try(localDateDecoder.decode("hello")).isFailure)
  }

  test("JsonDecoder LocalDate round-trip (with Gen)") {
    forAll(localDateGen) { (localDate: LocalDate) =>
      val json = "\"" + DateTimeFormatter.ISO_LOCAL_DATE.format(localDate) + "\""
      assert(localDateDecoder.decode(json) == localDate)
    }
  }

  test("JsonDecoder LocalDate round-trip (with Arbitrary)") {
    forAll { (localDate: LocalDate) =>
      val json = "\"" + DateTimeFormatter.ISO_LOCAL_DATE.format(localDate) + "\""
      assert(localDateDecoder.decode(json) == localDate)
    }
  }

  test("JsonDecoder orElse") {
    forAll { (json: String, number: Int, exception: Exception) =>
      val decoder1 = JsonDecoder.constant(number) orElse JsonDecoder.fail(exception)
      val decoder2 = JsonDecoder.fail(exception) orElse JsonDecoder.constant(number)

      assert(decoder1.decode(json) == number)
      assert(decoder2.decode(json) == number)
    }
  }

  test("JsonDecoder weirdLocalDateDecoder") {
    val date = LocalDate.of(2020, 3, 26)
    assert(weirdLocalDateDecoder.decode("\"2020-03-26\"") == date)
    assert(weirdLocalDateDecoder.decode("18347") == date)
    assert(Try(weirdLocalDateDecoder.decode("hello")).isFailure)
  }

  test("JsonDecoder Option") {
    assert(optionDecoder(stringDecoder).decode("null") == None)
    assert(optionDecoder(stringDecoder).decode("\"hello\"") == Some("hello"))
  }

  test("SafeJsonDecoder Int") {
    assert(SafeJsonDecoder.int.decode("1234") == Right(1234))
    assert(SafeJsonDecoder.int.decode("hello") == Left("Invalid JSON Int: hello"))
  }

  test("SafeJsonDecoder orElse") {
    val date = LocalDate.of(2020, 3, 26)
    assert(SafeJsonDecoder.localDate.decode("\"2020-03-26\"") == Right(date))
    assert(SafeJsonDecoder.localDate.decode("18347") == Right(date))
  }

  val localDateGen: Gen[LocalDate] =
    Gen
      .choose(LocalDate.MIN.toEpochDay, LocalDate.MAX.toEpochDay)
      .map(LocalDate.ofEpochDay)

  implicit val localDateArbitrary: Arbitrary[LocalDate] =
    Arbitrary(localDateGen)

  ///////////////////////////////
  // Exercise 4: Data processing
  ///////////////////////////////

  test("sum") {
    assert(sum(List(1, 5, 2)) == 8)
    assert(sum(List()) == 0)
  }

  test("sum is consistent with std library") {
    forAll { (numbers: List[Int]) =>
      assert(sum(numbers) == numbers.sum)
    }
  }

  test("sum concat") {
    forAll { (first: List[Int], second: List[Int]) =>
      assert((sum(first) + sum(second)) == sum(first ++ second))
    }
  }

  test("size") {
    assert(sum(List(1, 5, 2)) == 8)
    assert(sum(List()) == 0)
  }

  test("size is consistent with std library") {
    forAll { (numbers: List[Int]) =>
      assert(size(numbers) == numbers.size)
    }
  }

  test("size concat") {
    forAll { (first: List[Int], second: List[Int]) =>
      assert((size(first) + size(second)) == size(first ++ second))
    }
  }

  test("min") {
    assert(min(List(2, 5, 1, 8)) == Some(1))
    assert(min(Nil) == None)
  }

  test("min is lower than all values in the list") {
    forAll { (numbers: List[Int]) =>
      for {
        minValue <- min(numbers)
        number   <- numbers
      } assert(minValue <= number)
    }
  }

  test("min belongs to the list") {
    forAll { (numbers: List[Int]) =>
      for (minValue <- min(numbers))
        assert(numbers.contains(minValue))
    }
  }

  test("wordCount") {
    assert(wordCount(List("Hi", "Hello", "Hi")) == Map("Hi" -> 2, "Hello" -> 1))
    assert(wordCount(List()) == Map())
  }

  test("wordCount all counts are strictly greater than 0") {
    forAll { (words: List[String]) =>
      assert(wordCount(words).values.forall(_ > 0))
    }
  }

  test("all words are part of the result") {
    forAll { (words: List[String]) =>
      val keys = wordCount(words).keySet
      assert(words.forall(keys.contains))
    }
  }

  test("wordCount add 1") {
    forAll { (words: List[String], word: String) =>
      val result = wordCount(word :: words)
      wordCount(words).get(word) match {
        case None        => assert(result(word) == 1)
        case Some(count) => assert(result(word) == count + 1)
      }
    }
  }

  test("foldLeft is consistent with std library") {
    forAll { (numbers: List[Int], default: Int, combine: (Int, Int) => Int) =>
      assert(foldLeft(numbers, default)(combine) == numbers.foldLeft(default)(combine))
    }
  }

  test("foldLeft noop") {
    forAll { (numbers: List[Int]) =>
      assert(foldLeft(numbers, List.empty[Int])(_ :+ _) == numbers)
    }
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
