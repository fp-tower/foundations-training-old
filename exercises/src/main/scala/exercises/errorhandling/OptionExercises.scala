package exercises.errorhandling

import cats.effect.IO
import exercises.errorhandling.OptionExercises.Shape.{Circle, Rectangle}
import io.circe.{parser, Json}

import scala.util.Try

object OptionExercises {

  /////////////////////////
  // Exercise 1: Use cases
  /////////////////////////

  // 1a. Implement `getUserEmail` which looks up a user using its id,
  // then it returns the user's email if it exists.
  // val userMap = Map(
  //   222 -> User(222, "john" , Some("j@x.com")),
  //   123 -> User(123, "elisa", Some("e@y.com")),
  //   444 -> User(444, "bob", None)
  // )
  // getUserEmail(123, userMap) == Some("e@y.com")
  // getUserEmail(111, userMap) == None // no user
  // getUserEmail(444, userMap) == None // no email
  def getUserEmail(id: UserId, users: Map[UserId, User]): Option[Email] = ???

  case class User(id: UserId, name: String, email: Option[Email])
  case class UserId(value: Long)
  case class Email(value: String)

  sealed trait Role {

    // 1b. Implement `optSingleAccountId` which returns the account id if the role is a Reader or an Editor
    // such as Editor(123, "Comic Sans").optSingleAccountId == Some(123)
    //         Reader(123, premiumUser = true).optSingleAccountId == Some(123)
    // but     Admin.optSingleAccountId == None
    // Note: you can pattern match on Role using `this match { case Reader(...) => ... }`
    def optSingleAccountId: Option[AccountId] = ???

    // 1c. Implement `optEditor` which checks if the current `Role` is an `Editor`
    // such as Editor(123, "Comic Sans").optEditor == Some(Editor(123, "Comic Sans"))
    // but     Reader(123, premiumUser = true).optEditor == None
    def optEditor: Option[Role.Editor] = ???
  }

  object Role {
    // A Reader has a read-only access to a single account
    case class Reader(accountId: AccountId, premiumUser: Boolean) extends Role
    // An Editor has edit access to a single account
    case class Editor(accountId: AccountId, favoriteFont: String) extends Role
    // An Admin has complete power over all accounts
    case object Admin extends Role
  }

  case class AccountId(value: Long)

  ///////////////////////
  // GO BACK TO SLIDES
  ///////////////////////

  /////////////////////////
  // Exercise 2: Variance
  /////////////////////////

  sealed trait Shape extends Product with Serializable
  object Shape {
    case class Circle(radius: Int)                extends Shape
    case class Rectangle(width: Int, height: Int) extends Shape
  }

  def parseCircle(inputLine: String): InvOption[Circle] =
    inputLine.split(" ").toList match {
      case "C" :: IntParser(radius) :: Nil => InvOption.Some(Circle(radius))
      case _                               => InvOption.None()
    }

  // 2a. Implement `parseRectangle` which parses a user input line (e.g. from the command line) into a `Rectangle`.
  // `parseRectangle` expects the following format 'R', space, Int, space, Int, end of line
  // such as parseRectangle("R 20 5") == Some(Rectangle(20, 5))
  // but     parseRectangle("C 20 5") == None
  //         parseRectangle("R 0")    == None
  // Note: see `parseCircle` which parse a line into `Circle`.
  // Note: `parseRectangle` returns an `InvOption` which is like a standard `Option` but with an invariant type parameter.
  def parseRectangle(inputLine: String): InvOption[Rectangle] =
    ???

  // 2b. Implement `parseShape` which parses a user input line into a `Shape` (`Circle` or `Rectangle`).
  // Please REUSE `parseCircle` and `parseRectangle`.
  def parseShape(inputLine: String): InvOption[Shape] =
    ???

  object IntParser {
    def unapply(s: String): Option[Int] =
      Try(s.toInt).toOption
  }

  ///////////////////
  // 3. Advanced API
  ///////////////////

  // 3a. Implement `filterDigits` which only keeps the digits from the list
  // such as filterDigits(List('a', '1', 'b', 'c', '4')) == List(1, 4)
  // Note: use `charToDigit`.
  def filterDigits(characters: List[Char]): List[Int] =
    ???

  def charToDigit(char: Char): Option[Int] =
    char match {
      case '0' => Some(0)
      case '1' => Some(1)
      case '2' => Some(2)
      case '3' => Some(3)
      case '4' => Some(4)
      case '5' => Some(5)
      case '6' => Some(6)
      case '7' => Some(7)
      case '8' => Some(8)
      case '9' => Some(9)
      case _   => None
    }

  // 3b. Implement `checkAllDigits` which verifies that all input characters are digits
  // such as checkAllDigits(List('1', '2', '3')) == Some(List(1, 2, 3))
  // but     checkAllDigits(List('a', '1', 'b', 'c', '4')) == None
  // Note: you may want to use listSequence or listTraverse defined below.
  def checkAllDigits(characters: List[Char]): Option[List[Int]] =
    ???

  def listSequence[A](elements: List[Option[A]]): Option[List[A]] =
    elements.foldRight(Option(List.empty[A])) {
      case (Some(a), Some(state)) => Some(a :: state)
      case (None, _)              => None
      case (_, None)              => None
    }

  def listTraverse[A, B](xs: List[A])(f: A => Option[B]): Option[List[B]] =
    listSequence(xs.map(f))

  ///////////////////
  // 4. Limitation
  ///////////////////

  // 4a. Implement `sendUserEmail` which attempts to send an email to a user.
  // If the user is missing from the db, we will retry in 100 millis,
  // but if a user exists and it doesn't have an email address, then we fail the IO.
  // Can you reuse `getUserEmail`? Why?
  def sendUserEmail(db: DbApi, emailClient: EmailClient)(userId: UserId, emailBody: String): IO[Unit] =
    ???

  trait DbApi {
    def getAllUsers: IO[Map[UserId, User]]
  }

  trait EmailClient {
    def sendEmail(email: Email, body: String): IO[Unit]
  }

  // 4b. Implement `parsingJsonMessage` which attempts to parse a `String` into `Json` and returns either
  // a successful message in case of success (e.g. "OK") or
  // a descriptive error message in case of a failure (e.g. "invalid syntax at line 3: `foo :-: 4`'").
  // Note: assume you can only use `parseJson`.
  def parsingJsonMessage(jsonStr: String): String =
    ???

  def parseJson(jsonStr: String): Option[Json] =
    parser.parse(jsonStr).toOption
}
