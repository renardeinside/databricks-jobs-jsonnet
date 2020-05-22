package com.databricks.example

import org.scalatest.funsuite.AnyFunSuite

class FirstJobTest extends AnyFunSuite {
  test("run first job") {
    val job = FirstJob
    job.main(Array.empty)
  }
}
