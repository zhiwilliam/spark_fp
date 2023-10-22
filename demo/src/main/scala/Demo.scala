import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{Encoder, SparkSession}
import org.wzhi.framework.common.SparkBatch.simpleLocalSession
import org.wzhi.impls.config.DemoReadConfig
import org.wzhi.impls.config.DemoReadConfig._
import org.wzhi.impls.data.DataContainerImpls.BatchDatasetContainer
import org.wzhi.impls.read.SparkBatchReadTransaction
import org.wzhi.impls.read.SparkBatchReadTransaction._
import org.wzhi.layers.data.DataContainer
import org.wzhi.models.{DemoConfig, ReadCsvConfig, Transaction}
import pureconfig.ConfigSource

import java.sql.Timestamp
import java.time.Instant
import scala.reflect.ClassTag

object Demo {

  import cats._
  import cats.effect._
  import cats.mtl._
  import cats.syntax.all._
  import cats.data._
  import cats.effect.implicits._
  import cats.effect.unsafe.implicits.global

  type DATA = DataContainer[Transaction]

  def program[F[_] : Sync : NonEmptyParallel](implicit A: Ask[F, DATA]) = {
    for {
      inputData <- A.ask
      _ <- Sync[F].blocking {
        val transformed = inputData.map(x => x.copy(time = Timestamp.from(Instant.now)))
        transformed.outputToConsole
      }
    } yield ()
  }

  val materializedProgram = program[ReaderT[IO, DATA, *]]

  def main(args: Array[String]): Unit = {
    val materializedReader = SparkBatchReadTransaction.sparkBatchRead[ReaderT[IO, SPARK_CSV, *], Transaction]

    import pureconfig.generic.auto._
    val dependencies = (
      IO.blocking(simpleLocalSession("Demo Spark Read")),
      read[DemoConfig].fromFile("demo-config.conf")
    ).parTupled

    val process = for {
      (spark, config) <- dependencies
      readerDepends = (spark, config.readCsvConfig)
      data <- materializedReader.run(readerDepends)
      result <- materializedProgram.run(data)
    } yield()

    process.unsafeRunSync()
  }
}
