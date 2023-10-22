package org.wzhi.impls.read

import org.wzhi.models.ReadCsvConfig
import org.apache.spark.sql._
import org.wzhi.framework.DataFlow
import org.wzhi.framework.common.SparkBatch._
import org.wzhi.impls.data.DataContainerImpls.BatchDatasetContainer

import scala.reflect.runtime.universe.TypeTag

object SparkBatchReadTransaction {
  import cats._
  import cats.effect._
  import cats.mtl._
  import cats.syntax.all._

  type SPARK_CSV = (SparkSession, ReadCsvConfig)
  def sparkBatchRead[F[_] : Sync : NonEmptyParallel, T <: Product: TypeTag](
        implicit A: Ask[F, SPARK_CSV]): F[DataFlow[T]] = for {
    (sparkSession, readConfig) <- A.ask
    result <- Sync[F].blocking {
      import sparkSession.implicits._
      readCsv(readConfig.dataFilePath)(sparkSession).as[T]
    }
  } yield BatchDatasetContainer(result)(sparkSession)


}
