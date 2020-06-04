package com.databricks.example.dashboard

case class DashboardConfig(
                            events_source_path: String = null,
                            devices_source_path: String = null,
                            output_path: String = null,
                            checkpoint_location: String = null,
                            termination_ms: Option[Int] = None
                          ) {
  override def toString: String = {
    s"""
       |Dashboard Job config:
       | events_source_path=$events_source_path
       | devices_source_path=$devices_source_path
       | output_path=$output_path
       | checkpoint_location=$checkpoint_location
       | termination_ms=${termination_ms.getOrElse("NOT_SET")}
       |""".stripMargin
  }
}

