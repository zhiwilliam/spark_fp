import cats.data.Validated.{Invalid, Valid}
import io.scalaland.chimney.PartialTransformer
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import org.wzhi.framework._
import org.wzhi.framework.DataFlowImpls._
import org.wzhi.framework.common.SparkBatch
import org.wzhi.framework.common.SparkStream
import org.wzhi.core.parsers.StrParser._
import org.wzhi.core.validate.MkValidatedNel._
import pureconfig.generic.auto._
import org.wzhi.models._
import io.scalaland.chimney.dsl._
import io.scalaland.chimney.cats._
import org.wzhi.core.fileSystem.utils.folderOfFilePath
import org.wzhi.core.validate.nel.str.PredefinedValidators._
import org.wzhi.framework.common.SparkCreator.simpleLocalSession
import org.wzhi.framework.monads.FlatMapF

object Demo {
  import cats._
  import cats.effect._
  import cats.mtl._
  import cats.syntax.all._
  import org.wzhi.core.chimney_helpers.Compatiable._
  import cats.data._
  import cats.effect.implicits._
  import cats.effect.unsafe.implicits.global

  type DATA = (DataFlow[Transaction], DataStatic[Map[String, String]])

  // todo: tagless final version
  //  def program[F[_] : Sync : NonEmptyParallel, K[_]: FlatMapF](implicit A: Ask[F, DATA]): F[DataFlow[DemoResult]] = {
  def program[F[_] : Sync : NonEmptyParallel](implicit A: Ask[F, DATA]): F[DataFlow[DemoResult]] = {
    for {
      (inputData, enrichData) <- A.ask
      enriched <- Sync[F].blocking {
        inputData
          .map{ x =>
            val enrichMap = enrichData.value
            implicit val partialTransformer: PartialTransformer[Transaction, EnrichedTransaction] =
              PartialTransformer
                .define[Transaction, EnrichedTransaction]
                .withFieldComputedPartial(_.account, x => email.validate(x.account).toPartialResult)
                .withFieldComputedPartial(_.amount, x =>
                  (parseBigDec(x.amount).toNel(s"ID: ${x.id}'s amount"),
                    enrichMap.get(x.country).toNel(s"ID: ${x.id} can't find currency for ${x.country}"))
                    .mapN(Money).toPartialResult)
                .withFieldComputedPartial(_.isTest, x =>
                  parseBoolean(x.isTest).toNel(s"ID: ${x.id}'s isTest").toPartialResult)
                .buildTransformer

            x.transformIntoPartial[EnrichedTransaction].asValidatedNel.normalize
          }
      }
      demoResult <- Sync[F].pure(
        enriched.map {
          case Valid(a) => DemoResult(validated = true, value = Some(a))
          case Invalid(b) => DemoResult(validated = false, error = b.toList)
        }
      )
    } yield demoResult
  }

  def materializedProgram = program[ReaderT[IO, DATA, *]]

  def main(args: Array[String]): Unit = {
    import org.wzhi.framework.impls.config.PureConfigRead.read
    val dependencies = (
      IO.blocking(simpleLocalSession("Demo Spark Read")),
      read[DemoConfig].fromFile("demo-config.conf")
    ).parTupled

    val process = for {
      (spark, config) <- dependencies
      /*
      // Stream mode read csv folder
      data <- IO.blocking{
        import spark.implicits._
        implicit val session: SparkSession = spark
        StreamDatasetContainer(
          SparkStream.readCsv[Transaction](folderOfFilePath(config.readCsvConfig.dataFilePath)).as[Transaction])
      }*/

      // Spark mode read csv file
      data <- IO.blocking {
        import spark.implicits._
        implicit val session: SparkSession = spark
        // todo: SparkBatch functions can only use BatchDatastContainer. To avoid confuse, we need to develop
        // a implicit class for SparkBatch like asContainer[Transaction]. I am thinking of let readCsv returns
        // BatchDatasetContainer instead of Dataset...
        BatchDatasetContainer(
          SparkBatch.readCsv(folderOfFilePath(config.readCsvConfig.dataFilePath)).as[Transaction])
      }

      enrichData <- IO.pure {
        BroadCastStatic(spark.sparkContext.broadcast(Map("US" -> "USD")))
      }
      result <- {
        import FlatMapF._
        implicit val session: SparkSession = spark
        //implicit val datasetIsFlatMap = new DatasetFlatMap // tagless final
        materializedProgram.run((data, enrichData))
      }
    } yield result.outputToConsole

    process.unsafeRunSync()
  }
}
