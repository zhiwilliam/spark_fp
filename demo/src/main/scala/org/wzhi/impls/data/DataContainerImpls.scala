package org.wzhi.impls.data

import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import org.wzhi.layers.data.DataContainer

object DataContainerImpls {

  case class BatchDatasetContainer[A](data: Dataset[A]) extends DataContainer[A] {
    override def outputToConsole: Unit = {
      data.collect.foreach(println)
    }

    def flatMap[U: Encoder](f: A => IterableOnce[U]): DataContainer[U] =
      BatchDatasetContainer(data.flatMap(f))
  }
}
