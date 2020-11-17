package exercises.action

import java.util.concurrent.ExecutorService

import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.ExecutionContext

class IOAsyncExercisesTest extends AnyFunSuite with ScalaCheckDrivenPropertyChecks {

  /////////////////////////
  // 1. Concurrent Program
  /////////////////////////

  test("deleteTwoOrders") {
    withExecutionContext(ThreadPoolUtil.fixedSize(4, "deleteTwoOrders")) { ec =>
      }
  }

  test("deleteAllUserOrders") {}

  def withExecutionContext[A](makeES: => ExecutorService)(f: ExecutionContext => A): A = {
    val es = makeES
    val ec = ExecutionContext.fromExecutorService(es)
    val a  = f(ec)
    es.shutdown()
    a
  }

}
