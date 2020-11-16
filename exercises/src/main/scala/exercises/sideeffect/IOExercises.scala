package exercises.sideeffect

import java.time.Instant

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

object IOExercisesApp extends App {
  import IOExercises._

  unsafeConsoleProgram
}

object IOExercises {

  /////////////////////////
  // 1. Smart constructors
  /////////////////////////

  object IO {

    // 1a. Implement `succeed` a constructor of IO so that `unsafeRun` always returns the same value.
    // such as IO.succeed("Hello").unsafeRun() == "Hello"
    // and val dice = IO.succeed(Random.nextInt(6))
    // dice.unsafeRun() == dice.unsafeRun(), in other words, you would get the same number every time.
    def succeed[A](constant: A): IO[A] =
      new IO[A] {
        def unsafeRun(): A = ???
      }

    // 1b. Implement `effect` a constructor takes a block of code and evaluate it on every `unsafeRun`.
    // val greeting = IO.effect(println("hello")) // doesn't print anything
    // greeting.unsafeRun() // print "hello"
    // greeting.unsafeRun() // print "hello"
    def effect[A](block: => A): IO[A] =
      new IO[A] {
        def unsafeRun(): A = ???
      }

    // common alias for `succeed`
    // IO.pure(4) == IO.succeed(4)
    def pure[A](value: A): IO[A] =
      succeed(value)

    // common alias for `effect`
    // IO { println("hello") } instead of effect(println("hello"))
    def apply[A](fa: => A): IO[A] =
      effect(fa)

    // 1c. Implement `sleep` such as when run, the program do nothing for `duration` period of time,
    // then it will return `()` (the only value of type Unit)
    // see `Thread.sleep`
    def sleep(duration: FiniteDuration): IO[Unit] =
      ???

    // 1d. Implement `never` an IO that when you run it, blocks forever.
    // One way to see this is a program that sleeps forever.
    // What should be the return type of `never`

//  val never: IO[???]
  }

  /////////////////////
  // 2. IO API
  /////////////////////

  trait IO[A] {
    import IO._

    def unsafeRun(): A

    // 2a. Implement `map` so that we can transform the result of an IO.
    // Note that `map` takes a pure function as an input, you are not supposed to run a println or
    // throw an exception inside `update`.
    // such as IO.succeed(4).map(_ + 1) == IO.succeed(5)
    def map[B](update: A => B): IO[B] =
      ???

    // `void` discards the value returned by the IO.
    // Use case:
    // val rowsUpdated: IO[Int] = updateDb(sql"...")
    // val response: IO[Unit] = rowsUpdated.void
    def void: IO[Unit] =
      map(_ => ())

    // 2b. Implement `flatMap` which allows to do one action after another.
    // for example,
    // db.getUser(userId).flatMap{
    //   case None              => db.insertUser(user) // create a new user if none exist
    //   case Some(currentUser) => db.updateUser(user) // otherwise update existing user
    // }
    def flatMap[B](f: A => IO[B]): IO[B] =
      ???

    // `productL` and `productR` combines the effects of two IOs and discard the value of one them.
    // Use case:
    // logInfo("Fetching user $userId") *> getUser($userId)            : IO[User]  // logInfo value is discarded
    // CreateOrder(item, qty, userId) <* sendConfirmationEmail(userId) : IO[Order] // sendConfirmationEmail value is discarded.
    // Note that we can use a for comprehension because we already implemented `map` and `flatMap`.
    def productL[B](fb: IO[B]): IO[A] =
      for {
        a <- this
        _ <- fb
      } yield a

    def productR[B](fb: IO[B]): IO[B] =
      for {
        _ <- this
        b <- fb
      } yield b

    // common alias for `productL`
    def <*[B](fb: IO[B]): IO[A] =
      productL(fb)

    // common alias for `productR`
    def *>[B](fb: IO[B]): IO[B] =
      productR(fb)

    // 2c. Implement `attempt` which makes the error part of IO explicit.
    // Try[A] is either a Success(a: A) or a Failure(e: Throwable)
    // such as succeed(x).attempt == succeed(Success(x))
    //         fail(new Exception("")).attempt == succeed(Failure(new Exception(""))).
    // Note that `attempt` guarantees `unsafeRun()` will not throw an exception.
    def attempt: IO[Try[A]] =
      ???

    // 2d. Implement `handleErrorWith` which allows to catch a failing IO
    // such as fail(new Exception("")).handleErrorWith(_ => someIO) == someIO
    //         fail(new Exception("foo")).handleErrorWith{
    //            case e: IllegalArgumentException => succeed(1)
    //            case other                       => succeed(2)
    //         } == succeed(2)
    // Use case:
    // handleErrorWith(e => IO.effect(log.error("Operation failed"), e))
    def handleErrorWith(f: Throwable => IO[A]): IO[A] =
      ???

    // 2e. Implement `retryOnce` which re-runs the current IO if it fails.
    // Try first to use `attempt`
    def retryOnce: IO[A] =
      ???

    // 2f. Implement `retryUntilSuccess`
    // similar to `retryOnce` but it retries until the IO succeeds (potentially indefinitely)
    // sleep `waitBeforeRetry` between each retry
    // How would you update this method to implement an exponential back-off?
    def retryUntilSuccess(waitBeforeRetry: FiniteDuration): IO[A] = ???
  }

  ////////////////////
  // 3. Programs
  ////////////////////

  def unsafeReadLine: String =
    scala.io.StdIn.readLine()

  def unsafeWriteLine(message: String): Unit =
    println(message)

  // 3a. Implement `readLine` and `writeLine` such as it is a pure version of `unsafeReadLine` and `unsafeWriteLine`
  // Which smart constructor of IO should you use?
  // I used "lazy" to avoid throwing an exception while `readLine` is no implemented
  lazy val readLine: IO[String] =
    ???

  def writeLine(message: String): IO[Unit] =
    ???

  // 3b. Implement `consoleProgram` such as it is an IO version of `unsafeConsoleProgram`.
  // Try to re-use `readLine`, `writeLine` and IO combinators.
  lazy val consoleProgram: IO[String] =
    ???

  def unsafeConsoleProgram: String = {
    println("What's your name?")
    val name = scala.io.StdIn.readLine()
    println(s"Your name is $name")
    name
  }

  // 3d. Implement `userConsoleProgram` such as it is am IO version of `unsafeUserConsoleProgram`.
  // You may want to create helper methods to read an integer or the current time.
  case class User(name: String, age: Int, createdAt: Instant)

  lazy val userConsoleProgram: IO[User] =
    ???

  def unsafeUserConsoleProgram: User = {
    println("What's your name?")
    val name = scala.io.StdIn.readLine()
    println("What's your age?")
    val age = scala.io.StdIn.readLine().toInt
    User(name, age, createdAt = Instant.now())
  }

  // 3e. Implement `userConsoleWithRetryProgram`, a version of the previous program that retries
  // up to 3 times when reading the user's age.
  // For example, if the user enters "hello" <enter> "world" <enter> "23" <enter>, then the user's age will be 23
  // but if the user enter "hello" <enter> "world" <enter> "!" <enter>, then the program will fail.
  lazy val userConsoleWithRetryProgram: IO[User] =
    ???

  // 3f. How would you test these IO programs?
  // What are the issues with the current implementation?

  ////////////////////////
  // 4. Testing
  ////////////////////////

  trait Clock {
    val readNow: IO[Instant]
  }

  val systemClock: Clock = new Clock {
    val readNow: IO[Instant] = IO.effect(Instant.now())
  }

  // 4a. Implement `testClock` which facilitates testing of a Clock API.
  def testClock(constant: Instant): Clock = ???

  trait Console {
    val readLine: IO[String]
    def writeLine(message: String): IO[Unit]

    def readInt: IO[Int] = ???
  }

  val stdinConsole: Console = new Console {
    val readLine: IO[String]                 = IO.effect(scala.io.StdIn.readLine())
    def writeLine(message: String): IO[Unit] = IO.effect(println(message))
  }

  // 4b. Implement `testConsole` which facilitates testing of a Console API.
  // Use both `testClock` and `testConsole` to write a test for `userConsoleProgram2` in IOExercisesTest
  def testConsole(in: ListBuffer[String], out: ListBuffer[String]): Console =
    ???

  def userConsoleProgram2(console: Console, clock: Clock): IO[User] =
    for {
      _         <- console.writeLine("What's your name?")
      name      <- console.readLine
      _         <- console.writeLine("What's your age?")
      age       <- console.readInt
      createdAt <- clock.readNow
    } yield User(name, age, createdAt)

  // 4c. Now our production code is "pure" (free of side effect) but our test code is not.
  // How would you fix this?
  // Try to implement `safeTestConsole` such as it does perform any side effects or mutation.
  def safeTestConsole: Console = ???

  ////////////////////////
  // 5. Advanced API
  ////////////////////////

  // 5a. Implement `deleteTwoOrders` such as it call twice `UserOrderApi#deleteOrder`
  // How would you test `deleteTwoOrders`?
  def deleteTwoOrders(api: UserOrderApi)(orderId1: OrderId, orderId2: OrderId): IO[Unit] =
    ???

  // 5b. Implement `deleteAllUserOrders` such as it fetches a user: User_V2 and delete all associated orders
  // e.g. if `getUser` returns User_V2(UserId("1234"), "Rob", List(OrderId("1111"), OrderId("5555")))
  //      Then we would call deleteOrder(OrderId("1111")) and deleteOrder(OrderId("5555")).
  def deleteAllUserOrders(api: UserOrderApi)(userId: UserId): IO[Unit] =
    ???

  case class UserId(value: String)
  case class OrderId(value: String)

  case class User_V2(userId: UserId, name: String, orderIds: List[OrderId])

  trait UserOrderApi {
    def getUser(userId: UserId): IO[User_V2]
    def deleteOrder(orderId: OrderId): IO[Unit]
  }

  // 5c. Implement `sequence` which runs sequentially a list of IO and collects the results.
  // sequence(List(succeed(1), succeed(2))) == succeed(List(1,2))
  // sequence(List(succeed(1), notImplemented, succeed(3))) == succeed(1) *> notImplemented
  // use case:
  // val userIds: List[UserId] = ...
  // sequence(userIds.map(fetchUser)): IO[List[User]]
  def sequence[A](xs: List[IO[A]]): IO[List[A]] =
    ???

  // `traverse` captures a common use case of `map` followed by `sequence`
  // val userIds: List[UserId] = ...
  // def fetchUser(userId: UserId): IO[User] = ...
  // traverse(userIds)(fetchUser): IO[List[User]]
  def traverse[A, B](xs: List[A])(f: A => IO[B]): IO[List[B]] =
    sequence(xs.map(f))
}
