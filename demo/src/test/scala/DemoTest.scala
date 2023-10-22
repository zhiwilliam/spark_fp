import org.scalatest.flatspec.AnyFlatSpec
import org.wzhi.models.Transaction

import java.sql.Timestamp
import Demo._
import org.wzhi.framework.DataFlowImpls.ListContainer
class DemoTest extends AnyFlatSpec {
  "Demo" should "show local test" in {
    import cats.effect.unsafe.implicits.global

    val process = for {
      result <- materializedProgram.run(ListContainer(List(
        Transaction("id", "testAccount", Timestamp.valueOf("2023-01-01 23:21:56.344")))))
    } yield result.outputToConsole

    process.unsafeRunSync()
  }
}
