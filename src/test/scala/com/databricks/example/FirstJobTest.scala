package com.databricks.example

import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

class FirstJobTest extends AnyFunSuite {
  test("run first job") {
    val job = FirstJob

    job.spark = SparkSession.builder().master("local[*]").getOrCreate()

    job.main(Array.empty)

  }
}
