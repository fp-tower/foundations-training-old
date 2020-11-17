package exercises.action

import java.time.Instant

import exercises.action.IOAnswers._
import exercises.action.IORef
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.util.{Failure, Success, Try}

class IOAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  /////////////////////////
  // 1. Smart constructors
  /////////////////////////

  test("succeed") {
    assert(IO.succeed(4).unsafeRun() == 4)

    forAll { (x: Int) =>
      assert(IO.succeed(x).unsafeRun() == x)
    }
  }

  test("fail") {
    forAll { (e: Exception) =>
      assert(Try(IO.fail(e).unsafeRun()) == Failure(e))
    }
  }

  test("effect success") {
    forAll { (x: Int) =>
      assert(IO.effect(x).unsafeRun() == x)
    }
  }

  test("effect failure") {
    forAll { (e: Exception) =>
      assert(Try(IO.effect(throw e).unsafeRun()) == Failure(e))
    }
  }

  test("effect is lazy") {
    var called = false
    val io     = IO.effect { called = true }

    assert(called == false)
    io.unsafeRun()
    assert(called == true)
  }

  /////////////////////
  // 2. IO API
  /////////////////////

  test("map") {
    forAll { (x: Int, f: Int => Boolean) =>
      assert(IO.succeed(x).map(f).unsafeRun() == f(x))
    }
    forAll { (e: Exception, f: Int => Boolean) =>
      assert(IO.fail(e).map(f).attempt.unsafeRun() == Failure(e))
    }
  }

  test("flatMap") {
    forAll { (x: Int, f: Int => Int) =>
      assert(IORef(x).flatMap(_.updateGetNew(f)).unsafeRun() == f(x))
    }

    forAll { (e: Exception) =>
      assert(IO.fail(e).flatMap(_ => IO.notImplemented).attempt.unsafeRun() == Failure(e))
    }

    forAll { (x: Int, e: Exception) =>
      assert(IO.fail(e).flatMap(_ => IO.succeed(x)).attempt.unsafeRun() == Failure(e))
    }
  }

  test("*>") {
    val io = for {
      ref <- IORef(0)
      _   <- ref.update(_ + 1) *> ref.update(_ * 2)
      res <- ref.get
    } yield res

    assert(io.unsafeRun() == 2)
  }

  test("<*") {
    val io = for {
      ref <- IORef(0)
      _   <- ref.update(_ + 1) <* ref.update(_ * 2)
      res <- ref.get
    } yield res

    assert(io.unsafeRun() == 2)
  }

  test("attempt") {
    forAll { (x: Int) =>
      assert(IO.succeed(x).attempt.unsafeRun() == Success(x))
    }
    forAll { (e: Exception) =>
      assert(IO.fail(e).attempt.unsafeRun() == Failure(e))
    }
  }

  test("retry") {
    val error = new Exception("Unsupported odd number")
    def action(ref: IORef[Int]): IO[String] =
      ref.updateGetNew(_ + 1).map(_ % 2 == 0).flatMap {
        case true  => IO.succeed("OK")
        case false => IO.fail(error)
      }

    assert(IORef(0).flatMap(action).attempt.unsafeRun() == Failure(error))
    assert(IORef(0).flatMap(action(_).retry(1)).unsafeRun() == "OK")
  }

  ////////////////////
  // 4. Testing
  ////////////////////

  test("read user from Console") {
    val in: List[String] = List("John", "24")
    val now              = Instant.ofEpochMilli(100)
    val clock            = testClock(now)

    val tests = for {
      console <- safeTestConsole(in)
      user    <- userConsoleProgram2(console, clock)
      output  <- console.out.get
    } yield {
      assert(user == User("John", 24, now))
      assert(output == List("What's your name?", "What's your age?"))
    }

    tests.unsafeRun()
  }

  ////////////////////////
  // 5. Advanced API
  ////////////////////////

  test("traverse") {
    forAll { (xs: List[Int]) =>
      assert(traverse(xs)(IO.succeed).unsafeRun() == xs)
    }

    forAll { xs: List[Exception] =>
      val boom = new Exception("boom")
      assert(traverse(boom :: xs)(IO.fail).attempt.unsafeRun() == Failure(boom))
    }
  }

  test("deleteAllUserOrders") {
    val users = List(User_V2(UserId("1234"), "Rob", List(OrderId("1111"), OrderId("5555"))))
    def testApi(ref: IORef[List[OrderId]]) = new UserOrderApi {
      def getUser(userId: UserId): IO[User_V2] =
        users.find(_.userId == userId) match {
          case None    => IO.fail(new Exception(s"User not found $userId"))
          case Some(u) => IO.succeed(u)
        }

      def deleteOrder(orderId: OrderId): IO[Unit] =
        ref.update(_ :+ orderId)
    }

    val program = for {
      ref <- IORef(List.empty[OrderId])
      api = testApi(ref)
      _   <- deleteAllUserOrders(api)(UserId("1234"))
      ids <- ref.get
    } yield assert(ids == List(OrderId("1111"), OrderId("5555")))

    program.unsafeRun()
  }

}
