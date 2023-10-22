import org.apache.spark.sql.SparkSession
import org.wzhi.framework.DataFlow
import org.wzhi.framework.DataFlowImpls.BatchDatasetContainer
import org.wzhi.framework.common.SparkBatch.{readCsv, simpleLocalSession}
import org.wzhi.framework.impls.config.PureConfigRead._
import pureconfig.generic.auto._
import org.wzhi.models.{DemoConfig, Transaction}

import java.sql.Timestamp
import java.time.Instant

object Demo {

  import cats._
  import cats.effect._
  import cats.mtl._
  import cats.syntax.all._
  import cats.data._
  import cats.effect.implicits._
  import cats.effect.unsafe.implicits.global

  type DATA = DataFlow[Transaction]

  def program[F[_] : Sync : NonEmptyParallel](implicit A: Ask[F, DATA]): F[DataFlow[Transaction]] = {
    for {
      inputData <- A.ask
      result <- Sync[F].blocking {
        inputData
          .map(x => x.copy(time = Timestamp.from(Instant.now)))
      }
    } yield result
  }

  val materializedProgram = program[ReaderT[IO, DATA, *]]

  def main(args: Array[String]): Unit = {
    val dependencies = (
      IO.blocking(simpleLocalSession("Demo Spark Read")),
      read[DemoConfig].fromFile("demo-config.conf")
    ).parTupled

    val process = for {
      (spark, config) <- dependencies
      data <- IO.blocking{
        import spark.implicits._
        implicit val session: SparkSession = spark
        BatchDatasetContainer(readCsv(config.readCsvConfig.dataFilePath)(spark).as[Transaction])
      }
      result <- materializedProgram.run(data)
    } yield result.outputToConsole

    process.unsafeRunSync()
  }
}
