package org.wzhi.framework.common

import org.apache.spark.sql._

object SparkBatch {
  def readCsv(path: String, withHead: String = "true")(implicit spark: SparkSession): DataFrame =
    spark.read.option("header", withHead).option("inferSchema", "true").csv(path)

  // todo: Read from Kafka, delta table etc.

}
