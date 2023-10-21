package org.wzhi.framework.common

import org.apache.spark.sql._

object SparkBatch {
  def simpleLocalSession(appName: String) = {
    SparkSession.builder()
      .master("local[1]")
      .appName(appName)
      .getOrCreate();
  }

  def readCsv(path: String)(implicit spark: SparkSession): DataFrame =
    spark.read.option("header","true").option("inferSchema", "true").csv(path)

}
