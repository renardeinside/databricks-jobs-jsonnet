package com.databricks.example.devices

case class DevicesConfig(source_path: String = null, output_path: String = null) {
  override def toString: String = {
    s"""
       |Devices Job config:
       | source_path=$source_path
       | output_path=$output_path
       |""".stripMargin
  }
}

