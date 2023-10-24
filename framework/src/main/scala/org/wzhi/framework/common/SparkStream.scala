package org.wzhi.framework.common

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Encoder, SparkSession}

object SparkStream {
  def readCsv[T: Encoder](path: String, withHead: String = "true")(implicit spark: SparkSession): DataFrame = {
    val encoder: Encoder[T] = implicitly
    implicit val schema = encoder.schema
    spark.readStream.option("header", withHead).schema(schema).option("inferSchema", "true").csv(path)
  }
}
