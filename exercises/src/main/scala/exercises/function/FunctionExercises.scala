package exercises.function

import java.time.LocalDate

import exercises.function.HttpClientBuilder
import exercises.function.HttpClientBuilder._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.Random

// you can run and print things here
object FunctionApp extends App {
  import FunctionExercises._

  println("Hello World!")
}

object FunctionExercises {

  /////////////////////////////////////////////////////
  // Exercise 1: String API with higher-order functions
  /////////////////////////////////////////////////////

  // 1a. Implement `selectDigits` which iterates over a String and only keep the characters that are digits.
  // such as selectDigits("hello4world-80") == "480"
  // but     selectDigits("welcome") == ""
  // Note: You can use `filter` method from `String`, also check out the API of `Char`
  def selectDigits(text: String): String =
    ???

  // 1b. Implement `secret` which transforms all characters in a String to '*'
  // such as secret("Welcome123") == "**********"
  // Note: Try to use a higher-order function from the String API
  def secret(text: String): String =
    ???

  // 1c. Implement `isValidUsernameCharacter` which checks if a character is suitable for a username.
  // We accept:
  // - lower and upper case letters
  // - digits
  // - special characters: '-' and '_'
  // For example, isValidUsernameCharacter('3') == true
  //              isValidUsernameCharacter('a') == true
  // but          isValidUsernameCharacter('^') == false
  // Note: You might find some useful helper methods on `char`.
  def isValidUsernameCharacter(char: Char): Boolean =
    ???

  // 1d. Implement `isValidUsername` which checks that all the characters in a String are valid
  // such as isValidUsername("john-doe") == true
  // but     isValidUsername("*john*") == false
  // Note: Try to use `isValidUsernameCharacter` and a higher-order function from the String API.
  def isValidUsername(username: String): Boolean =
    ???

  ///////////////////////
  // Exercise 2: Point
  ///////////////////////

  case class Point(x: Int, y: Int, z: Int) {
    // 2a. Implement `isPositive` which returns true if `x`, `y` and `z` are all greater or equal to 0, false otherwise
    // such as Point(2, 4,9).isPositive == true
    //         Point(0, 0,0).isPositive == true
    // but     Point(0,-2,1).isPositive == false
    // Note: `isPositive` is a function defined within `Point` class, so `isPositive` has access to `x`, `y` and `z`.
    def isPositive: Boolean =
      ???

    // 2b. Implement `isEven` which returns true if `x`, `y` and `z` are all even numbers, false otherwise
    // such as Point(2, 4, 8).isEven == true
    //         Point(0,-8,-2).isEven == true
    // but     Point(3,-2, 0).isEven == false
    // Note: You can use `% 2` to check if a number is odd or even,
    // e.g. 8 % 2 == 0 but 7 % 2 == 1
    def isEven: Boolean =
      ???

    // 2c. Both `isPositive` and `isEven` check that a predicate holds for `x`, `y` and `z`.
    // Let's try to capture this pattern with a higher order function like `forAll`
    // such as Point(1,1,1).forAll(_ == 1) == true
    // but     Point(1,2,5).forAll(_ == 1) == false
    // Then, re-implement `isPositive` and `isEven` using `forAll`
    def forAll(predicate: Int => Boolean): Boolean =
      ???
  }

  ////////////////////////////
  // Exercise 3: JsonDecoder
  ////////////////////////////

  // very basic representation of JSON
  type Json = String

  trait JsonDecoder[A] {
    def decode(json: Json): A
  }

  val intDecoder: JsonDecoder[Int] = new JsonDecoder[Int] {
    def decode(json: Json): Int = json.toInt
  }

  val stringDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    def decode(json: Json): String =
      if (json.startsWith("\"") && json.endsWith("\"")) // check it starts and ends with `"`
        json.substring(1, json.length - 1)
      else
        throw new IllegalArgumentException(s"$json is not a valid JSON string")
  }

  // SAM syntax for JsonDecoder
  val intDecoderSAM: JsonDecoder[Int] =
    (json: Json) => json.toInt

  // 3a. Implement `userIdDecoder`, a `JsonDecoder` for the `UserId` case class
  // such as userIdDecoder.decode("1234") == UserId(1234)
  // but     userIdDecoder.decode("hello") would throw an Exception
  case class UserId(value: Int)
  lazy val userIdDecoder: JsonDecoder[UserId] =
    ???

  // 3b. Implement `localDateDecoder`, a `JsonDecoder` for `LocalDate`
  // such as localDateDecoder.decode("\"2020-03-26\"") == LocalDate.of(2020,3,26)
  // but     localDateDecoder.decode("2020-03-26") would throw an Exception
  // and     localDateDecoder.decode("hello") would throw an Exception
  // Note: You can parse a `LocalDate` using `LocalDate.parse` with a java.time.format.DateTimeFormatter
  // e.g. DateTimeFormatter.ISO_LOCAL_DATE
  lazy val localDateDecoder: JsonDecoder[LocalDate] =
    ???

  // 3c. Implement `map` a generic function that converts a `JsonDecoder` of `From`
  // into a `JsonDecoder` of `To`.
  // Bonus: Can you re-implement `userIdDecoder` and `localDateDecoder` using `map`
  def map[From, To](decoder: JsonDecoder[From], update: From => To): JsonDecoder[To] =
    ???

  // 3d. Move `map` inside of `JsonDecoder` trait so that we can use the syntax
  // `intDecoder.map(_ + 1)` instead of `map(intDecoder)(_ + 1)`

  // 3e. Imagine we have to integrate with a weird JSON API where dates are sometimes encoded
  // using a String with the format "yyyy-mm-dd" and sometimes they are encoded using
  // JSON numbers representing the number of days since the epoch. For example,
  // weirdLocalDateDecoder.decode("\"2020-03-26\"") == LocalDate.of(2020,3,26)
  // weirdLocalDateDecoder.decode("18347")          == LocalDate.of(2020,3,26)
  // but weirdLocalDateDecoder.decode("hello") would throw an Exception
  // Try to think how we could extend JsonDecoder so that we can easily implement
  // other decoders that follow the same pattern.
  lazy val weirdLocalDateDecoder: JsonDecoder[LocalDate] =
    ???

  ///////////////////////////
  // 3. Recursion & Laziness
  ///////////////////////////

  // 3a. Implement `sumList` using an imperative approach (while, for loop)
  // such as sumList(List(1,5,2)) == 8
  def sumList(xs: List[Int]): Int =
    ???

  // 3b. Implement `mkString` using an imperative approach (while, for loop)
  // such as mkString(List('H', 'e', 'l', 'l', 'o')) == "Hello"
  def mkString(xs: List[Char]): String =
    ???

  // 3c. Implement `sumList2` using recursion (same behaviour than `sumList`).
  // Does your implementation work with a large list? e.g. List.fill(1000000)(1)
  def sumList2(xs: List[Int]): Int =
    ???

  ///////////////////////
  // GO BACK TO SLIDES
  ///////////////////////

  def foldLeft[A, B](fa: List[A], b: B)(f: (B, A) => B): B = {
    var acc = b
    for (a <- fa) {
      acc = f(acc, a)
    }
    acc
  }

  @tailrec
  def foldLeftRec[A, B](xs: List[A], b: B)(f: (B, A) => B): B =
    xs match {
      case Nil => b
      case h :: t =>
        val newB = f(b, h)
        foldLeftRec(t, newB)(f)
    }

  def sumList3(xs: List[Int]): Int =
    foldLeft(xs, 0)(_ + _)

  // 3d. Implement `mkString2` using `foldLeft` (same behaviour than `mkString`)
  def mkString2(xs: List[Char]): String =
    ???

  // 3e. Implement `multiply` using `foldLeft`
  // such as multiply(List(3,2,4)) == 3 * 2 * 4 = 24
  // and     multiply(Nil) == 1
  def multiply(xs: List[Int]): Int =
    ???

  // 3f. Implement `forAll` which checks if all elements in a List are true
  // such as forAll(List(true, true , true)) == true
  // but     forAll(List(true, false, true)) == false
  // does your implementation terminate early? e.g. forAll(List(false, false, false)) does not go through the entire list
  // does your implementation work with a large list? e.g. forAll(List.fill(1000000)(true))
  def forAll(xs: List[Boolean]): Boolean =
    ???

  // 3g. Implement `find` which returns the first element in a List where the predicate answers.function returns true
  // such as find(List(1,3,10,2,6))(_ > 5) == Some(10)
  // but     find(List(1,2,3))(_ > 5) == None
  // does your implementation terminate early? e.g. find(List(1,2,3,4)(_ == 2) stop iterating as soon as it finds 2
  // does your implementation work with a large list? e.g. find(1.to(1000000).toList)(_ == -1)
  def find[A](xs: List[A])(predicate: A => Boolean): Option[A] =
    ???

  ///////////////////////
  // GO BACK TO SLIDES
  ///////////////////////

  def foldRight[A, B](xs: List[A], b: B)(f: (A, => B) => B): B =
    xs match {
      case Nil    => b
      case h :: t => f(h, foldRight(t, b)(f))
    }

  // 3h. Implement `forAll2` using `foldRight` (same behaviour than `forAll`)
  def forAll2(xs: List[Boolean]): Boolean =
    ???

  // 3i. Implement `headOption` using `foldRight`.
  // `headOption` returns the first element of a List if it exists
  // such as headOption(List(1,2,3)) == Some(1)
  // but     headOption(Nil) == None
  def headOption[A](xs: List[A]): Option[A] =
    ???

  // 3j. What fold (left or right) would you use to implement `min`? Why?
  def min(xs: List[Int]): Option[Int] = ???

  // 3k. Run `isEven` or `isOdd` for small and large input.
  // Search for mutual tail recursion in Scala.
  def isEvenRec(x: Int): Boolean =
    if (x > 0) isOddRec(x - 1)
    else if (x < 0) isOddRec(x + 1)
    else true

  def isOddRec(x: Int): Boolean =
    if (x > 0) isEvenRec(x - 1)
    else if (x < 0) isEvenRec(x + 1)
    else false

  // 3l. What happens when we call `foo`? Search for General recursion
  // or read https://www.quora.com/Whats-the-big-deal-about-recursion-without-a-terminating-condition
  def foo: Int = foo

  ////////////////////////
  // 4. Pure functions
  ////////////////////////

  // 4a. is `plus` a pure answers.function? why?
  def plus(a: Int, b: Int): Int = a + b

  // 4b. is `div` a pure answers.function? why?
  def div(a: Int, b: Int): Int =
    if (b == 0) sys.error("Cannot divide by 0")
    else a / b

  // 4c. is `times2` a pure answers.function? why?
  var counterTimes2 = 0
  def times2(i: Int): Int = {
    counterTimes2 += 1
    i * 2
  }

  // 4d. is `boolToInt` a pure answers.function? why?
  def boolToInt(b: Boolean): Int =
    if (b) 5
    else Random.nextInt() / 2

  // 4e. is `mapLookup` a pure answers.function? why?
  def mapLookup(map: Map[String, Int], key: String): Int =
    map(key)

  // 4f. is `times3` a pure answers.function? why?
  def times3(i: Int): Int = {
    println("do something here") // could be a database access or http call
    i * 3
  }

  // 4g. is `circleArea` a pure answers.function? why?
  val pi = 3.14
  def circleArea(radius: Double): Double =
    radius * radius * pi

  // 4h. is `inc` or inc_v2 a pure answers.function? why?
  def inc_v2(xs: Array[Int]): Unit =
    for { i <- xs.indices } xs(i) = xs(i) + 1

  // 4i. is `incAll` a pure answers.function? why?
  def incAll(value: Any): Any = value match {
    case x: Int    => x + 1
    case x: Long   => x + 1
    case x: Double => x + 1
  }

  // 4j. is `sum` a pure answers.function? why?
  def sum(xs: List[Int]): Int = {
    var acc = 0
    xs.foreach(x => acc += x)
    acc
  }

  ////////////////////////
  // 5. Memoization
  ////////////////////////

  // 5a. Implement `memoize` such as
  // val cachedInc = memoize((_: Int) + 1)
  // cachedInc(3) // 4 calculated
  // cachedInc(3) // from cache
  // see https://medium.com/musings-on-functional-programming/scala-optimizing-expensive-functions-with-memoization-c05b781ae826
  // or https://github.com/scalaz/scalaz/blob/series/7.3.x/tests/src/test/scala/scalaz/MemoTest.scala
  def memoize[A, B](f: A => B): A => B = ???

  // 5b. How would you adapt `memoize` to work on recursive answers.function e.g. fibonacci
  // can you generalise the pattern?
  def memoize2 = ???

}
