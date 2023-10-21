import com.typesafe.config.ConfigFactory
import org.wzhi.framework.common.SparkBatch.simpleLocalSession
import org.wzhi.impls.config.DemoReadConfig
import org.wzhi.impls.config.DemoReadConfig._
import org.wzhi.impls.read.SparkBatchReadTransaction
import org.wzhi.impls.read.SparkBatchReadTransaction._
import org.wzhi.layers.data.DataContainer
import org.wzhi.models.{DemoConfig, ReadCsvConfig, Transaction}
import pureconfig.ConfigSource

import scala.reflect.ClassTag

object Demo {
  def main(args: Array[String]) = {
    import cats._
    import cats.effect._
    import cats.mtl._
    import cats.syntax.all._
    import cats.data._
    import cats.effect.implicits._
    import cats.effect.unsafe.implicits.global

    type DATA = DataContainer[Transaction]
    def program[F[_]: Sync: NonEmptyParallel](implicit A: Ask[F, DATA]) = {
      for {
        dataContainer <- A.ask
        _ <- Sync[F].blocking {
          dataContainer .outputToConsole
        }
      } yield ()
    }

    val materializedReader = SparkBatchReadTransaction.sparkBatchRead[ReaderT[IO, SPARK_CSV, *]]
    val materializedProgram = program[ReaderT[IO, DATA, *]]

    val dependencies = (
      IO.blocking(simpleLocalSession("Demo Spark Read")),
      IOConfig("demo-config.conf").read
    ).parTupled

    val process = for {
      (spark, config) <- dependencies
      readerDepends = (spark, config.readCsvConfig)
      data <- materializedReader.run(readerDepends)
      result <- materializedProgram.run(data)
    } yield()


    import org.wzhi.impls.config.DemoReadConfig._

    process.unsafeRunSync()
  }
}
