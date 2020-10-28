package exercises.sideeffect

import java.util.concurrent.ExecutorService

import exercises.sideeffect.{IOAsync, IOAsyncRef, IORef}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import exercises.sideeffect.ThreadPoolUtil.CounterExecutionContext

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class IOAsyncAnswersTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  test("succeed") {
    assert(IOAsync.succeed(4).unsafeRun() == 4)

    forAll { (x: Int) =>
      assert(IOAsync.succeed(x).unsafeRun() == x)
    }
  }

  test("fail") {
    forAll { (e: Exception) =>
      assert(Try(IOAsync.fail(e).unsafeRun()) == Failure(e))
    }
  }

  test("effect success") {
    forAll { (x: Int) =>
      assert(IOAsync.effect(x).unsafeRun() == x)
    }
  }

  test("effect failure") {
    forAll { (e: Exception) =>
      assert(Try(IOAsync.effect(throw e).unsafeRun()) == Failure(e))
    }
  }

  test("effect is lazy") {
    var called = false
    val io     = IOAsync.effect { called = true }

    assert(called == false)
    io.unsafeRun()
    assert(called == true)
  }

  test("fromFuture") {
    var counter     = 0
    lazy val future = Future { counter += 1 }(scala.concurrent.ExecutionContext.global)

    val io = IOAsync.fromFuture(future)
    assert(counter == 0)

    io.unsafeRun()
    assert(counter == 1)
  }

  test("traverse-evalOn") {
    withExecutionContext(ThreadPoolUtil.fixedSize(4, "traverse-evalOn")) { ec =>
      val counterEC = new CounterExecutionContext(ec)

      def bump(ref: IOAsyncRef[Int]): IOAsync[Unit] =
        (IOAsync.printThreadName *> IOAsync.sleep(100.milliseconds) *> ref.update(_ + 1)).evalOn(counterEC)

      val io = for {
        ref <- IOAsyncRef(0)
        _   <- IOAsync.printThreadName
        _   <- IOAsync.traverse(List.fill(10)(0))(_ => bump(ref))
        _   <- IOAsync.printThreadName
        res <- ref.get
      } yield res

      assert(io.unsafeRun() == 10)
      assert(counterEC.executeCalled.get() == 10)
    }
  }

  test("concurrentTraverse") {
    withExecutionContext(ThreadPoolUtil.fixedSize(4, "concurrentTraverse-evalOn")) { ec =>
      val counterEC = new CounterExecutionContext(ec)

      def bump(ref: IOAsyncRef[Int]): IOAsync[Unit] =
        IOAsync.printThreadName *> IOAsync.sleep(500.milliseconds) *> ref.update(_ + 1)

      val io = for {
        ref <- IOAsyncRef(0)
        _   <- IOAsync.printThreadName
        _   <- IOAsync.concurrentTraverse(List.fill(10)(0))(_ => bump(ref))(counterEC)
        _   <- IOAsync.printThreadName
        res <- ref.get
      } yield res

      assert(io.unsafeRun() == 10)
      assert(counterEC.executeCalled.get() == 10)
    }
  }

  test("concurrentMap2") {
    withExecutionContext(ThreadPoolUtil.fixedSize(4, "concurrentMap2")) { ec =>
      val counterEC = new CounterExecutionContext(ec)

      def bump(ref: IOAsyncRef[Int]): IOAsync[Unit] =
        IOAsync.printThreadName *> ref.updateGetNew(_ + 1).map(_.toString).flatMap(IOAsync.printLine)

      val io = for {
        ref <- IOAsyncRef(0)
        _   <- bump(ref).concurrentTuple(bump(ref))(counterEC)
        res <- ref.get
      } yield res

      assert(io.unsafeRun() == 2)
      assert(counterEC.executeCalled.get() == 2)
    }
  }

  test("evalOn - async") {
    withExecutionContext(ThreadPoolUtil.fixedSize(2, "ec1")) { ec1 =>
      withExecutionContext(ThreadPoolUtil.fixedSize(2, "ec2")) { ec2 =>
        withExecutionContext(ThreadPoolUtil.fixedSize(2, "ec3")) { ec3 =>
          withExecutionContext(ThreadPoolUtil.fixedSize(2, "ec4")) { ec4 =>
            val asyncPrint = IOAsync.async[Unit](cb => cb(Right(println("cb: " + Thread.currentThread.getName))))(ec4)
            val io         = asyncPrint *> IOAsync.effect(println("io: " + Thread.currentThread.getName))

            (io.evalOn(ec1) *> io.evalOn(ec2)).evalOn(ec3).unsafeRun()
          }
        }
      }
    }
  }

  test("map") {
    forAll { (x: Int, f: Int => Boolean) =>
      assert(IOAsync.succeed(x).map(f).unsafeRun() == f(x))
    }
    forAll { (e: Exception, f: Int => Boolean) =>
      assert(IOAsync.fail(e).map(f).attempt.unsafeRun() == Left(e))
    }
  }

  test("flatMap") {
    forAll { (x: Int, f: Int => Int) =>
      assert(IORef(x).flatMap(_.updateGetNew(f)).unsafeRun() == f(x))
    }

    forAll { (e: Exception) =>
      assert(IOAsync.fail(e).flatMap(_ => IOAsync.notImplemented).attempt.unsafeRun() == Left(e))
    }

    forAll { (x: Int, e: Exception) =>
      assert(IOAsync.fail(e).flatMap(_ => IOAsync.succeed(x)).attempt.unsafeRun() == Left(e))
    }
  }

  test("attempt") {
    forAll { (x: Int) =>
      assert(IOAsync.succeed(x).attempt.unsafeRun() == Right(x))
    }

    forAll { (e: Exception) =>
      assert(IOAsync.fail(e).attempt.unsafeRun() == Left(e))
    }
  }

  test("retryOnce") {
    val error = new Exception("Unsupported odd number")
    def action(ref: IOAsyncRef[Int]): IOAsync[String] =
      ref.updateGetNew(_ + 1).map(_ % 2 == 0).flatMap {
        case true  => IOAsync.succeed("OK")
        case false => IOAsync.fail(error)
      }

    assert(IOAsyncRef(0).flatMap(action).attempt.unsafeRun() == Left(error))
    assert(IOAsyncRef(0).flatMap(action(_).retryOnce).unsafeRun() == "OK")
  }

  // TODO use Resource or handmade equivalent
  def withExecutionContext[A](makeES: => ExecutorService)(f: ExecutionContext => A): A = {
    val es = makeES
    val ec = ExecutionContext.fromExecutorService(es)
    val a  = f(ec)
    es.shutdown()
    a
  }

}
