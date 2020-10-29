package exercises.function

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

  ////////////////////////////
  // 2. polymorphic functions
  ////////////////////////////

  case class Pair[A](first: A, second: A) {
    def map[B](f: A => B): Pair[B] =
      Pair(f(first), f(second))
  }

  def mapOption[A, B](option: Option[A], f: A => B): Option[B] =
    option match {
      case None    => None
      case Some(a) => Some(f(a))
    }

  def identity[A](x: A): A = x

  def const[A, B](a: A)(b: B): A = a

  def setOption[A, B](option: Option[A])(value: B): Option[B] =
    option.map(const(value))

  def andThen[A, B, C](f: A => B, g: B => C): A => C =
    a => g(f(a))

  def compose[A, B, C](f: B => C, g: A => B): A => C =
    a => f(g(a))

  val inc: Int => Int    = x => x + 1
  val double: Int => Int = x => 2 * x

  val doubleInc: Int => Int = andThen(double, inc)

  val incDouble: Int => Int = compose(double, inc)

  val default: HttpClientBuilder = HttpClientBuilder.default("localhost", 8080)

  val clientBuilder1: HttpClientBuilder = default
    .withTimeout(10.seconds)
    .withFollowRedirect(true)
    .withMaxParallelRequest(3)

  val clientBuilder2: HttpClientBuilder =
    (withTimeout(10.seconds) compose
      withFollowRedirect(true) compose
      withMaxParallelRequest(3)).apply(default)

  ///////////////////////////
  // 3. Recursion & Laziness
  ///////////////////////////

  def sumList(xs: List[Int]): Int = {
    var sum = 0
    for (x <- xs) sum += x
    sum
  }

  def sumList2(xs: List[Int]): Int = {
    @tailrec
    def _sumList(ys: List[Int], acc: Int): Int =
      ys match {
        case Nil    => acc
        case h :: t => _sumList(t, acc + h)
      }

    _sumList(xs, 0)
  }

  def sumList3(xs: List[Int]): Int =
    foldLeft(xs, 0)(_ + _)

  def mkString(xs: List[Char]): String = {
    var str = ""
    for (x <- xs) str += x
    str
  }

  def mkString2(xs: List[Char]): String =
    foldLeft(xs, "")(_ + _)

  @tailrec
  def foldLeft[A, B](xs: List[A], z: B)(f: (B, A) => B): B =
    xs match {
      case Nil    => z
      case h :: t => foldLeft(t, f(z, h))(f)
    }

  def reverse[A](xs: List[A]): List[A] =
    foldLeft(xs, List.empty[A])(_.::(_))

  def multiply(xs: List[Int]): Int = foldLeft(xs, 1)(_ * _)

  def filter[A](xs: List[A])(p: A => Boolean): List[A] =
    foldLeft(xs, List.empty[A])((acc, a) => if (p(a)) a :: acc else acc).reverse

  def foldRight[A, B](xs: List[A], z: B)(f: (A, => B) => B): B =
    xs match {
      case Nil    => z
      case h :: t => f(h, foldRight(t, z)(f))
    }

  @tailrec
  def find[A](fa: List[A])(p: A => Boolean): Option[A] =
    fa match {
      case Nil     => None
      case x :: xs => if (p(x)) Some(x) else find(xs)(p)
    }

  @tailrec
  def forAll(fa: List[Boolean]): Boolean =
    fa match {
      case Nil        => true
      case false :: _ => false
      case true :: xs => forAll(xs)
    }

  def forAll2(xs: List[Boolean]): Boolean =
    foldRight(xs, true)(_ && _)

  def headOption[A](xs: List[A]): Option[A] =
    foldRight(xs, Option.empty[A])((a, _) => Some(a))

  def find2[A](xs: List[A])(p: A => Boolean): Option[A] =
    foldRight(xs, Option.empty[A])((a, rest) => if (p(a)) Some(a) else rest)

  def min(xs: List[Int]): Option[Int] =
    xs match {
      case Nil          => None
      case head :: tail => Some(foldLeft(tail, head)(_ min _))
    }

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
