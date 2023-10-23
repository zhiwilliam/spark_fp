import org.scalatest.flatspec.AnyFlatSpec
import org.wzhi.models.Transaction

import java.sql.Timestamp
import Demo._
import org.wzhi.framework.DataFlowImpls.{ListContainer, ListStatic}
class DemoTest extends AnyFlatSpec {
  "Demo" should "show local test" in {
    import cats.effect.unsafe.implicits.global

    val process = for {
      result <- materializedProgram.run(ListContainer(List(
        Transaction("1234", "testAccount", "27.56", "US", Timestamp.valueOf("2023-01-01 23:21:56.344"), "true"))),
        ListStatic(Map("US" -> "USD")))

    } yield result.outputToConsole

    process.unsafeRunSync()
  }
}
