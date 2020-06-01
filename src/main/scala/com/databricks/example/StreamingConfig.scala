package com.databricks.example

case class StreamingConfig(
                            source_path: String = null,
                            output_path: String = null,
                            checkpoint_location: String = null,
                            termination_ms: Option[Int] = None
                          )

