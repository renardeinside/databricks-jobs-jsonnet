package com.databricks.example.devices

import org.apache.spark.internal.Logging
import org.apache.spark.sql.types.{DoubleType, LongType, StructType}
import org.apache.spark.sql.{SaveMode, SparkSession}

object DevicesJob extends App with Logging {

  log.info("Argument parsing step initiated")

  val parser = new scopt.OptionParser[DevicesConfig]("streaming-job") {
    opt[String]("source_path") required() action { (x, c) =>
      c.copy(source_path = x)
    }
    opt[String]("output_path") required() action { (x, c) =>
      c.copy(output_path = x)
    }
  }

  val conf = parser.parse(args, DevicesConfig()).getOrElse {
    throw new Exception(s"Incorrect arguments passed to the job!")
  }

  log.info(s"Arguments successfully parsed, job configuration: $conf")

  // this shall be a var because we need to inject other classes as a dependency in tests
  var spark = SparkSession.builder().getOrCreate()


  val expectedSchema = new StructType()
    .add("device_id", LongType)
    .add("lat", DoubleType)
    .add("lon", DoubleType)

  val devicesDF = spark
    .read
    .schema(expectedSchema)
    .format("csv")
    .option("header", "true")
    .load(conf.source_path)


  devicesDF
    .write
    .format("delta")
    .mode(SaveMode.Overwrite)
    .save(conf.output_path)

  log.info("Devices job successfully stopped!")
}
