package com.databricks.example.dashboard

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

object DashboardJob extends App with Logging {

  log.info("Argument parsing step initiated")

  val parser = new scopt.OptionParser[DashboardConfig]("streaming-job") {
    opt[String]("events_source_path") required() action { (x, c) =>
      c.copy(events_source_path = x)
    }
    opt[String]("devices_source_path") required() action { (x, c) =>
      c.copy(devices_source_path = x)
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

  val conf = parser.parse(args, DashboardConfig()).getOrElse {
    throw new Exception(s"Incorrect arguments passed to the job!")
  }

  log.info(s"Arguments successfully parsed, job configuration: $conf")

  // this shall be a var because we need to inject other classes as a dependency in tests
  var spark = SparkSession.builder().getOrCreate()

  val eventsStream = spark
    .readStream
    .format("delta")
    .load(conf.events_source_path)

  val devices = spark.read.format("delta").load(conf.devices_source_path)

  val transformedStream = eventsStream
    .join(devices, eventsStream("device_id") === devices("device_id"))
    .drop(devices("device_id"))


  val writerQuery = transformedStream
    .writeStream
    .format("delta")
    .option("checkpointLocation", conf.checkpoint_location)
    .trigger(Trigger.ProcessingTime("5 seconds"))
    .start(conf.output_path)


  conf.termination_ms match {
    case None =>
      log.info("Starting unbounded streaming job")
      writerQuery.awaitTermination()
    case Some(timeout) =>
      log.info(s"Starting bounded streaming job with timeout $timeout")
      writerQuery.awaitTermination(timeout)
      log.info("Timeout requirement fulfilled, gracefully stopping the stream")
      spark.streams.active.foreach(_.stop())
      log.info("All streams stopped")
  }

  log.info("Streaming job successfully stopped!")
}
