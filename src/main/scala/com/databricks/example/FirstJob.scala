package com.databricks.example

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

object FirstJob extends App with Logging {

  var spark = SparkSession.builder().getOrCreate()

  val result = spark.sparkContext.parallelize(0 to 1000).sum()

  log.info(s"Result is $result")

}
