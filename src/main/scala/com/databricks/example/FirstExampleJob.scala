package com.databricks.example

import org.apache.spark.sql.SparkSession

object FirstExampleJob extends App {

  val spark = SparkSession.builder().master("local[*]").getOrCreate()

  spark.sparkContext.parallelize(0 to 1000).sum()

}
