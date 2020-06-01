package com.databricks.example.streaming

case class StreamingConfig(
                            source_path: String = null,
                            output_path: String = null,
                            checkpoint_location: String = null,
                            termination_ms: Option[Int] = None
                          ) {
  override def toString: String = {
    s"""
       |Streaming Job config:
       | source_path=$source_path
       | output_path=$output_path
       | checkpoint_location=$checkpoint_location
       | termination_ms=${termination_ms.getOrElse("NOT_SET")}
       |""".stripMargin
  }
}

