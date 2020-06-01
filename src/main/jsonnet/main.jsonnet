local jobName = "itrusov-jsonnet-demo-first-job-%s-%s" % [std.extVar("branch"), std.extVar("version")];

local commonConf = {
  "name": jobName,
  "new_cluster": {
    "spark_version": "6.5.x-scala2.11",
    "instance_pool_id": "0530-115052-mauve9-pool-uB4Kg5Xy",
    "num_workers": 0 #initial value, should be overriden by following configurations
  },
  "libraries": [
    {
      "jar": "dbfs:/mnt/jars/%s.jar" % [jobName]
    }
  ],
  "timeout_seconds": 3600,
  "max_retries": 0,
  "spark_jar_task": {
    "main_class_name": "com.databricks.example.StreamingJob"
  }
};

local testConf = commonConf + {
    "new_cluster"+: {
        "num_workers": 1
    },
    "spark_jar_task" +: {
        "parameters": [
            "--source_path", "/databricks-datasets/structured-streaming/events",
            "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/test/data/bronze-events",
            "--checkpoint_location", "/mnt/ivan.trusov@databricks.com/examples/test/jsonnet/checkpoints/bronze-events",
            "--timeout_ms", "100000" # 100 seconds
            ]
    }
};

local liveConf = commonConf + {
    "new_cluster"+: {"num_workers": 4},
    "spark_jar_task" +: {
            "parameters": [
                "--source_path", "/databricks-datasets/structured-streaming/events",
                "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/live/data/bronze-events",
                "--checkpoint_location", "/mnt/ivan.trusov@databricks.com/examples/live/jsonnet/checkpoints/bronze-events"
                ]
        }
};

{
    'test-conf.json': testConf,
    'live-conf.json': liveConf
}
