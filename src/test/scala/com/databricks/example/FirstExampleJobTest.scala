package com.databricks.example

import org.scalatest.funsuite.AnyFunSuite

class FirstExampleJobTest extends AnyFunSuite {
  test("run first job") {
    val job = FirstExampleJob
    job.main(Array.empty)
  }
}
