package com.databricks.example.devices

case class DevicesConfig(output_path: String = null) {
  override def toString: String = {
    s"""
       |Devices Job config:
       | output_path=$output_path
       |""".stripMargin
  }
}

