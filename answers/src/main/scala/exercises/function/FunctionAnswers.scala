package exercises.function

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import exercises.function.HttpClientBuilder._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object FunctionAnswers {

  /////////////////////////////////////////////////////
  // Exercise 1: String API with higher-order functions
  /////////////////////////////////////////////////////

  def selectDigits(text: String): String =
    text.filter(_.isDigit)

  def secret(text: String): String =
    text.map(_ => '*')

  def isValidUsernameCharacter(char: Char): Boolean =
    char.isLetterOrDigit || (char == '-') || (char == '_')

  def isValidUsername(username: String): Boolean =
    username.forall(isValidUsernameCharacter)

  ///////////////////////
  // Exercise 2: Point3
  ///////////////////////

  case class Point(x: Int, y: Int, z: Int) {
    def isPositive: Boolean =
      x >= 0 && y >= 0 && z >= 0

    def isEven: Boolean =
      (x % 2 == 0) && (y % 2 == 0) && (z % 2 == 0)

    def forAll(predicate: Int => Boolean): Boolean =
      predicate(x) && predicate(y) && predicate(z)

    def isPositiveForAll: Boolean =
      forAll(_ >= 0)

    def isEvenForAll: Boolean =
      forAll(_ % 2 == 0)
  }

  ////////////////////////////
  // Exercise 3: JsonDecoder
  ////////////////////////////

  // very basic representation of JSON
  type Json = String

  trait JsonDecoder[A] { outer =>
    def decode(json: Json): A

    def map[To](update: A => To): JsonDecoder[To] =
      new JsonDecoder[To] {
        def decode(json: Json): To =
          update(outer.decode(json))
      }

    def orElse(fallback: JsonDecoder[A]): JsonDecoder[A] =
      (json: Json) =>
        Try(outer.decode(json)) match {
          case Failure(_)     => fallback.decode(json)
          case Success(value) => value
      }
  }

  object JsonDecoder {
    def constant[A](value: A): JsonDecoder[A] = new JsonDecoder[A] {
      def decode(json: Json): A = value
    }

    def fail[A](exception: Exception): JsonDecoder[A] = new JsonDecoder[A] {
      def decode(json: Json): A =
        throw exception
    }
  }

  val intDecoder: JsonDecoder[Int] = new JsonDecoder[Int] {
    def decode(json: Json): Int = json.toInt
  }

  val stringDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    def decode(json: Json): String =
      if (json.startsWith("\"") && json.endsWith("\""))
        json.substring(1, json.length - 1)
      else
        throw new IllegalArgumentException(s"$json is not a JSON string")
  }

  case class UserId(value: Int)
  val userIdDecoder: JsonDecoder[UserId] =
    (json: Json) => UserId(intDecoder.decode(json))

  val localDateDecoder: JsonDecoder[LocalDate] =
    (json: Json) => LocalDate.parse(stringDecoder.decode(json), DateTimeFormatter.ISO_LOCAL_DATE)

  def map[From, To](decoder: JsonDecoder[From])(update: From => To): JsonDecoder[To] =
    (json: Json) => update(decoder.decode(json))

  val userIdDecoderV2: JsonDecoder[UserId] =
    intDecoder.map(UserId)

  val localDateDecoderV2: JsonDecoder[LocalDate] =
    stringDecoder.map(LocalDate.parse(_, DateTimeFormatter.ISO_LOCAL_DATE))

  val longDecoder: JsonDecoder[Long] =
    (json: Json) => json.toLong

  val longLocalDateDecoder: JsonDecoder[LocalDate] =
    longDecoder.map(LocalDate.ofEpochDay)

  val weirdLocalDateDecoder: JsonDecoder[LocalDate] =
    localDateDecoderV2 orElse longLocalDateDecoder

  def optionDecoder[A](decoder: JsonDecoder[A]): JsonDecoder[Option[A]] = {
    case "null" => None
    case other  => Some(decoder.decode(other))
  }

  trait SafeJsonDecoder[A] { self =>
    def decode(json: Json): Either[String, A]

    def map[To](update: A => To): SafeJsonDecoder[To] =
      new SafeJsonDecoder[To] {
        def decode(json: Json): Either[String, To] =
          self.decode(json).map(update)
      }

    def orElse(other: SafeJsonDecoder[A]): SafeJsonDecoder[A] =
      new SafeJsonDecoder[A] {
        def decode(json: Json): Either[String, A] =
          self.decode(json).orElse(other.decode(json))
      }
  }

  object SafeJsonDecoder {
    val int: SafeJsonDecoder[Int] =
      (json: Json) => Try(json.toInt).toOption.toRight(s"Invalid JSON Int: $json")

    val long: SafeJsonDecoder[Long] =
      (json: Json) => Try(json.toLong).toOption.toRight(s"Invalid JSON Long: $json")

    val string: SafeJsonDecoder[String] =
      (json: Json) =>
        if (json.startsWith("\"") && json.endsWith("\""))
          Right(json.substring(1, json.length - 1))
        else
          Left(s"$json is not a JSON string")

    val localDateInt: SafeJsonDecoder[LocalDate] =
      long.map(LocalDate.ofEpochDay)

    val localDateString: SafeJsonDecoder[LocalDate] =
      string.map(LocalDate.parse(_, DateTimeFormatter.ISO_LOCAL_DATE))

    val localDate: SafeJsonDecoder[LocalDate] =
      localDateString.orElse(localDateInt)
  }

  ///////////////////////////////
  // Exercise 4: Data processing
  ///////////////////////////////

  def sum(numbers: List[Int]): Int = {
    var total = 0
    for (x <- numbers) total += x
    total
  }

  def size[A](elements: List[A]): Int = {
    var counter = 0
    for (_ <- elements) counter += 1
    counter
  }

  def min(numbers: List[Int]): Option[Int] = {
    var state = Option.empty[Int]
    for (number <- numbers) state = combineMin(state, number)
    state
  }

  private def combineMin(state: Option[Int], number: Int): Option[Int] =
    state match {
      case None               => Some(number)
      case Some(currentState) => Some(currentState min number)
    }

  def wordCount(words: List[String]): Map[String, Int] = {
    var state = Map.empty[String, Int]
    for (word <- words) state = addKey(state, word)
    state
  }

  def addKey[K](state: Map[K, Int], key: K): Map[K, Int] =
    state.updatedWith(key) {
      case None    => Some(1)
      case Some(n) => Some(n + 1)
    }

  def foldLeft[From, To](elements: List[From], default: To)(combine: (To, From) => To): To = {
    var state = default
    for (element <- elements) state = combine(state, element)
    state
  }

  // 4e. Implement `diskUsage` a function that calculates the size of file/directory in bytes.
  // Please try to implement `diskUsage` using an imperative approach (loop + variables).
  // Note: check the `length` and `listFiles` methods on `File`.
  def diskUsage(input: File): Long = {
    var total = 0L
    val queue = mutable.Queue(input)

    while (queue.nonEmpty) {
      val file = queue.dequeue()
      total += file.length()

      if (file.isDirectory)
        queue.addAll(file.listFiles())
    }

    total
  }

  // 4f. Implement `diskUsageRecursive` another version of `diskUsage` but this time using recursions.
  def diskUsageRecursive(file: File): Long =
    if (file.isDirectory)
      file.length() + file.listFiles.map(diskUsage).sum
    else file.length()

  ////////////////////////
  // 5. Memoization
  ////////////////////////

  def memoize[A, B](f: A => B): A => B = {
    val cache = mutable.Map.empty[A, B]
    (a: A) =>
      {
        cache.get(a) match {
          case Some(b) => b // cache succeeds
          case None =>
            val b = f(a)
            cache.update(a, b) // update cache
            b
        }
      }
  }
}
