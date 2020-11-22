package exercises.function

import java.io.File
import java.time.{Instant, LocalDate}

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

  // 1a. Implement `secret` which transforms all characters in a String to '*'
  // such as secret("Welcome123") == "**********"
  // Note: Try to use a higher-order function from the String API
  //       You can test this function in FunctionExercisesTest.scala
  def secret(text: String): String =
    ???

  def isValidUsernameCharacter(char: Char): Boolean =
    char.isLetterOrDigit || char == '-' || char == '_'

  // 1b. Implement `isValidUsername` which checks that all the characters in a String are valid
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

  ///////////////////////////////
  // Exercise 4: Data processing
  ///////////////////////////////

  // 4a. Implement `size` using a mutable state and a for loop
  // such as sum(List(2,5,-3,8)) == 12
  // and     sum(Nil) == 0
  def sum(numbers: List[Int]): Int =
    ???

  // 4b. Implement `min` using a mutable state and a for loop
  // such as min(List(2,5,1,8)) == Some(1)
  // and     min(Nil) == None
  // Note: Option is an enumeration with two values:
  // * Some when there is a value and
  // * None when there is no value (a bit like null)
  def min(numbers: List[Int]): Option[Int] =
    ???

  // 4c. Implement `wordCount` using a mutable state and a for loop.
  // `wordCount` compute how many times each word appears in a `List`
  // such as wordCount(List("Hi", "Hello", "Hi")) == Map("Hi" -> 2, "Hello" -> 1)
  // and     wordCount(Nil) == Map.empty
  // Note: You can lookup an element in a `Map` with the method `get`
  // and you can upsert a value using `updated`
  def wordCount(words: List[String]): Map[String, Int] =
    ???

  // 4d. `sum`, `min` and `wordCount` are quite similar.
  // Could you write a higher-order function that captures this pattern?
  // How would you call it?
  def pattern = ???

  // 4e. Implement `diskUsage` a function that calculates the size of file/directory in bytes.
  // Please try to implement `diskUsage` using an imperative approach (loop + variables).
  // Note: check the `length` and `listFiles` methods on `File`.
  def diskUsage(input: File): Long =
    ???

  // 4f. Implement `diskUsageRecursive` another version of `diskUsage` but this time using recursions.
  def diskUsageRecursive(input: File): Long =
    ???

  /////////////////////////////////
  // Exercise 5: Functional subset
  /////////////////////////////////

  // 5a. does `plus` respect the functional subset? why?
  def plus(a: Int, b: Int): Int = a + b

  // 5b. does `createPost` respect the functional subset? why?
  case class BlogPost(title: String, content: String, createdAt: Instant)

  def createPost(title: String, content: String): BlogPost =
    if (title.isEmpty || content.isEmpty)
      throw new IllegalArgumentException("Title or content must not be empty")
    else BlogPost(title, content, Instant.now())

  // 5c. does `max` respect the functional subset? why?
  def max[A](list: List[Int]): Option[Int] = {
    var state = Option.empty[Int]
    for (element <- list)
      state match {
        case None             => state = Some(element)
        case Some(currentMax) => if (currentMax < element) state = Some(element)
      }

    state
  }

  // 5d. does `getOrder` and `insertOrder` respect the functional subset? why?
  case class Order(clientId: UserId, orderId: Long, product: String, price: Double)
  private var orders: Map[Long, Order] = Map.empty

  def getOrder(clientId: UserId, orderId: Long): Option[Order] =
    orders.get(orderId).filter(_.clientId == clientId)

  def insertOrder(order: Order): Unit =
    orders = orders.updated(order.orderId, order)

  // 5e. does `times3` respect the functional subset? why?
  def times3(number: Int): Int = {
    println(s"Times 3 called with input $number")
    number * 3
  }

  // 5f. does `circleArea` respect the functional subset? why?
  val pi = 3.14
  def circleArea(radius: Double): Double =
    radius * radius * pi

  // 5g. does `increment` respect the functional subset? why?
  def increment(xs: Array[Int]): Unit =
    for { i <- xs.indices } xs(i) = xs(i) + 1

  // 5h. does `incAll` respect the functional subset? why?
  def incAll(value: Any): Any = value match {
    case x: Int    => x + 1
    case x: Long   => x + 1
    case x: Double => x + 1
  }

  /////////////////////////////////
  // Exercise 6: Memoization
  /////////////////////////////////

  // 6a. Implement `memoize` such as
  // val cachedInc = memoize((_: Int) + 1)
  // cachedInc(3) // 4 calculated
  // cachedInc(3) // from cache
  // see https://medium.com/musings-on-functional-programming/scala-optimizing-expensive-functions-with-memoization-c05b781ae826
  // or https://github.com/scalaz/scalaz/blob/series/7.3.x/tests/src/test/scala/scalaz/MemoTest.scala
  def memoize[A, B](fun: A => B): A => B = ???

  // 6b. How would you adapt `memoize` to work on recursive answers.function e.g. fibonacci
  // can you generalise the pattern?
  def memoizeV2 = ???

}
