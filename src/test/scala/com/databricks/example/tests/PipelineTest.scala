package com.databricks.example.tests

import java.nio.file.Files

import com.databricks.example.dashboard.DashboardJob
import com.databricks.example.devices.DevicesJob
import com.databricks.example.streaming.StreamingJob
import com.databricks.example.tests.PipelineTest._
import com.databricks.example.utils.SparkSupport
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, LongType, StructType, TimestampType}
import org.scalatest.funsuite.AnyFunSuite

class PipelineTest extends AnyFunSuite with SparkSupport {


  val tempDir: String = Files.createTempDirectory("pipeline-test").toFile.getPath

  val streamingSourcePath: String = s"$tempDir/streaming/source"
  val streamingOutputPath: String = s"$tempDir/streaming/output"

  val streamingSourceCheckpointLocation: String = s"$tempDir/checkpoints/streaming/source"
  val streamingOutputCheckpointLocation: String = s"$tempDir/checkpoints/streaming/output"

  val devicesSourcePath: String = DevicesJob.getClass.getClassLoader.getResource("device_location.csv").getPath
  val devicesOutputPath: String = s"$tempDir/devices/output"

  val dashboardOutputPath: String = s"$tempDir/dashboard/output"
  val dashboardOutputCheckpointLocation: String = s"$tempDir/checkpoints/dashboard/output"

  test("run streaming job") {

    startTestingStream(spark, streamingSourcePath, streamingSourceCheckpointLocation)

    val job = StreamingJob

    job.spark = spark

    job.main(Array(
      "--source_path", streamingSourcePath,
      "--output_path", streamingOutputPath,
      "--checkpoint_location", streamingOutputCheckpointLocation,
      "--termination_ms", (10 * 1000).toString // wait 10 seconds to verify the result
    ))

    val resultDF = spark.read.format("delta").load(streamingOutputPath)

    val expectedSchema = new StructType()
      .add("device_id", LongType)
      .add("registered_value", DoubleType)
      .add("report_timestamp", TimestampType)

    assert(resultDF.count() > 0)
    assert(resultDF.schema == expectedSchema)

  }

  test("run devices job") {

    val job = DevicesJob
    job.spark = spark

    job.main(Array(
      "--source_path", devicesSourcePath,
      "--output_path", devicesOutputPath
    ))

    val resultDF = spark.read.format("delta").load(devicesOutputPath)

    assert(resultDF.count() > 0)
  }

  test("run dashboard job") {
    val job = DashboardJob
    job.spark = spark

    job.main(Array(
      "--events_source_path", streamingOutputPath,
      "--devices_source_path", devicesOutputPath,
      "--output_path", dashboardOutputPath,
      "--checkpoint_location", dashboardOutputCheckpointLocation,
      "--termination_ms", (10 * 1000).toString // wait 10 seconds to verify the result
    ))

    val resultDF = spark.read.format("delta").load(dashboardOutputPath)
    assert(resultDF.count() > 0)
  }
}

object PipelineTest {
  def startTestingStream(spark: SparkSession, path: String, checkpointLocation: String): Unit = {
    spark
      .readStream
      .format("rate")
      .option("rowsPerSecond", 1)
      .load()
      .withColumn("time", col("timestamp").cast("bigint"))
      .select("time")
      .writeStream
      .format("json")
      .option("checkpointLocation", checkpointLocation)
      .start(path)
  }
}