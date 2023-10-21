package org.wzhi.impls.read

import org.wzhi.models.ReadCsvConfig
import cats.effect.Sync
import org.apache.spark.sql._
import org.wzhi.layers.data.DataContainer
import org.wzhi.layers.read.ReadData
import org.wzhi.models.Transaction
import org.wzhi.framework.common.SparkBatch._
import org.wzhi.impls.data.DataContainerImpls.BatchDatasetContainer

object SparkBatchReadTransaction {
  import cats._
  import cats.effect._
  import cats.mtl._
  import cats.syntax.all._

  type SPARK_CSV = (SparkSession, ReadCsvConfig)
  def sparkBatchRead[F[_] : Sync : NonEmptyParallel](implicit A: Ask[F, SPARK_CSV]): F[DataContainer[Transaction]] = for {
    (sparkSession, readConfig) <- A.ask
    result <- Sync[F].blocking {
      import sparkSession.implicits._
      readCsv(readConfig.dataFilePath)(sparkSession).as[Transaction]
    }
  } yield BatchDatasetContainer(result)
}
