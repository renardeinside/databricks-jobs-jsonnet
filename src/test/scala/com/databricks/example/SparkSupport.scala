package com.databricks.example

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SparkSupport extends BeforeAndAfterAll {
  self: Suite =>

  var spark: SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    spark = SparkSession
      .builder()
      .appName("unit-testing")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", 1)
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    spark.stop()
  }
}
