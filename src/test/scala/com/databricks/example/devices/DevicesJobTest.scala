package com.databricks.example.devices

import java.nio.file.Files

import com.databricks.example.utils.SparkSupport
import org.scalatest.funsuite.AnyFunSuite

class DevicesJobTest extends AnyFunSuite with SparkSupport {

  val tempDir: String = Files.createTempDirectory("devices-job").toFile.getPath
  val outputPath: String = s"$tempDir/output"
  val sourcePath: String = DevicesJob.getClass.getClassLoader.getResource("device_location.csv").getPath

  test("run devices job") {

    val job = DevicesJob
    job.spark = spark

    job.main(Array(
      "--source_path", sourcePath,
      "--output_path", outputPath
    ))

  }
}
