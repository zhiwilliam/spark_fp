package org.wzhi.framework.common

import org.apache.spark.sql.SparkSession

object SparkCreator {
  def simpleLocalSession(appName: String) = {
    val spark = SparkSession.builder()
      .master("local[1]")
      .appName(appName)
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    spark
  }
}
