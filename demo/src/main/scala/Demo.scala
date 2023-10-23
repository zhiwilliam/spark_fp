import cats.data.Validated.{Invalid, Valid}
import io.scalaland.chimney.PartialTransformer
import org.apache.spark.sql.SparkSession
import org.wzhi.framework.{DataFlow, DataStatic}
import org.wzhi.framework.DataFlowImpls.{BatchDatasetContainer, BroadCastStatic}
import org.wzhi.framework.common.SparkBatch.{readCsv, simpleLocalSession}
import org.wzhi.framework.impls.config.PureConfigRead._
import org.wzhi.core.parsers.StrParser._
import org.wzhi.core.validate.MkValidatedNel._
import pureconfig.generic.auto._
import org.wzhi.models._

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

  def program[F[_] : Sync : NonEmptyParallel](implicit A: Ask[F, DATA]) = {
    for {
      (inputData, enrichData) <- A.ask
      enriched <- Sync[F].blocking {
        inputData
          .map{ x =>
            import io.scalaland.chimney.dsl._
            import io.scalaland.chimney.cats._

            import org.wzhi.core.validate.nel.str.PredefinedValidators._
            import org.wzhi.core.validate.nel.str.ValidatorStrErr
            import ValidatorStrErr._
            import ValidatorStrErr.strNelValidator._

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
      enrichData <- IO.blocking {
        BroadCastStatic(spark.sparkContext.broadcast(Map("US" -> "USD")))
      }
      result <- materializedProgram.run((data, enrichData))
    } yield result.outputToConsole

    process.unsafeRunSync()
  }
}
