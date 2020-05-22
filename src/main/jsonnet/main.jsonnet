local commonConf = {
  "name": "first-job-%s-%s" % [std.extVar("branch"), std.extVar("version")],
  "new_cluster": {
    "spark_version": "6.5.x-scala2.11",
    "node_type_id": "Standard_D3_v2",
    "num_workers": 1
  },
  "libraries": [
    {
      "jar": "dbfs:/mnt/jars/first-job-%s-%s.jar" % [std.extVar("branch"), std.extVar("version")]
    }
  ],
  "timeout_seconds": 3600,
  "max_retries": 1,
  "spark_jar_task": {
    "main_class_name": "com.databricks.example.FirstJob"
  },
  "instance_pool_id": "0522-223301-hoof20-pool-l59GfzQZ"
};

local testConf = commonConf + {
    "new_cluster"+: {"num_workers": 2},
};

local liveConf = commonConf + {
    "new_cluster"+: {"num_workers": 4},
};

{
    'test-conf.json': testConf,
    'live-conf.json': liveConf
}
