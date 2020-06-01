package com.databricks.example

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, floor, rand}

object StreamingJob extends App with Logging {

  log.info("Argument parsing step initiated")

  val parser = new scopt.OptionParser[StreamingConfig]("streaming-job") {
    opt[String]("source_path") required() action { (x, c) =>
      c.copy(source_path = x)
    }
    opt[String]("output_path") required() action { (x, c) =>
      c.copy(output_path = x)
    }
    opt[String]("checkpoint_location") required() action { (x, c) =>
      c.copy(checkpoint_location = x)
    }
    opt[Option[Int]]("termination_ms") action { (x, c) =>
      c.copy(termination_ms = x)
    }
  }

  val conf = parser.parse(args, StreamingConfig()).getOrElse {
    throw new Exception(s"Incorrect arguments passed to the job!")
  }

  log.info("Arguments successfully parsed!")

  // these shall be a var because we need to inject other classes as a dependency in tests
  var spark = SparkSession.builder().getOrCreate()

  val rawSource = spark
    .readStream
    .format("json")
    .schema("time bigint")
    .load(conf.source_path)

  val transformedStream = rawSource
    .withColumn("device_id", floor(rand() * 2))
    .withColumn("registered_value", rand())
    .withColumn("report_timestamp", col("time").cast("timestamp"))
    .drop("action", "time")


  val writerQuery = transformedStream
    .writeStream
    .format("delta")
    .option("checkpointLocation", conf.checkpoint_location)
    .start(conf.output_path)


  conf.termination_ms match {
    case None => writerQuery.awaitTermination()
    case Some(x) => writerQuery.awaitTermination(x)
  }


}