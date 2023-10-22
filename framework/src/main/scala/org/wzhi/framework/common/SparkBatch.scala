package org.wzhi.framework.common

import org.apache.spark.sql._

object SparkBatch {
  def simpleLocalSession(appName: String) = {
    SparkSession.builder()
      .master("local[1]")
      .appName(appName)
      .getOrCreate();
  }

  def readCsv(path: String, withHead: String = "true")(implicit spark: SparkSession): DataFrame =
    spark.read.option("header", withHead).option("inferSchema", "true").csv(path)

}
