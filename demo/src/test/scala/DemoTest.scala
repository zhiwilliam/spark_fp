import org.scalatest.flatspec.AnyFlatSpec
import org.wzhi.models._

import java.sql.Timestamp
import Demo._
import org.scalatest.matchers.should.Matchers._
import org.wzhi.framework.DataFlowImpls.{ListContainer, ListStatic}
class DemoTest extends AnyFlatSpec {
  "Demo" should "successfully local test" in {
    import cats.effect.unsafe.implicits.global

    val process = materializedProgram.run(ListContainer(List(
        Transaction("1234", "test@gmail.com", "27.48", "US", Timestamp.valueOf("2023-01-01 23:21:56.344"), "true"))),
        ListStatic(Map("US" -> "USD")))

    val test = process.unsafeRunSync()
    test.outputToConsole
    test.headOption.map(x => x shouldBe DemoResult(true, List(), Some(EnrichedTransaction("1234", "test@gmail.com",
      Money(27.48,"USD"), Timestamp.valueOf("2023-01-01 23:21:56.344"),true))))
  }

  "Demo" should "show all error messages" in {
    import cats.effect.unsafe.implicits.global

    val process = materializedProgram.run(ListContainer(List(
      Transaction("1234", "testAccount", "wrong", "CA", Timestamp.valueOf("2023-01-01 23:21:56.344"), "Nan"))),
      ListStatic(Map("US" -> "USD")))

    val test = process.unsafeRunSync()
    test.outputToConsole
  }
}
