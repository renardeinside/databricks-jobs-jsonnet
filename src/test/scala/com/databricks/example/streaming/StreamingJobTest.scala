package com.databricks.example.streaming

import java.nio.file.Files

import com.databricks.example.streaming.StreamingJobTest.startTestingStream
import com.databricks.example.utils.SparkSupport
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, LongType, StructType, TimestampType}
import org.scalatest.funsuite.AnyFunSuite

class StreamingJobTest extends AnyFunSuite with SparkSupport {

  val tempDir: String = Files.createTempDirectory("streaming-job").toFile.getPath

  val sourcePath: String = s"$tempDir/source"
  val outputPath: String = s"$tempDir/output"
  val sourceCheckpointLocation = s"$tempDir/checkpoints/source"
  val outputcheckpointLocation: String = s"$tempDir/checkpoints/output"

  test("run streaming job") {

    startTestingStream(spark, sourcePath, sourceCheckpointLocation)

    val job = StreamingJob

    job.spark = spark

    job.main(Array(
      "--source_path", sourcePath,
      "--output_path", outputPath,
      "--checkpoint_location", outputcheckpointLocation,
      "--termination_ms", (10 * 1000).toString // wait 10 seconds to verify the result
    ))

    val resultDF = spark.read.format("delta").load(outputPath)

    val expectedSchema = new StructType()
      .add("device_id", LongType)
      .add("registered_value", DoubleType)
      .add("report_timestamp", TimestampType)

    assert(resultDF.count() > 0)
    assert(resultDF.schema == expectedSchema)

  }

}

object StreamingJobTest {
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
