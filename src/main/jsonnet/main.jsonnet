local userName = "ivan.trusov";

local jarName = "%s-jsonnet-demo-%s-%s" % [userName, std.extVar("branch"), std.extVar("version")];

local commonConf = {
  "name": "",
  "new_cluster": {
    "spark_version": "6.5.x-scala2.11",
    "instance_pool_id": "0530-115052-mauve9-pool-uB4Kg5Xy",
    "num_workers": 0 #initial value, should be overriden by following configurations
  },
  "libraries": [
    {
      "jar": "dbfs:/mnt/jars/%s.jar" % [jarName]
    }
  ],
  "timeout_seconds": 3600,
  "max_retries": 0,
  "spark_jar_task": {
    "main_class_name": ""
  }
};

local testConf = commonConf + {
    "envName": "test",
    "new_cluster"+: {"num_workers": 1}
};

local liveConf = commonConf + {
    "envName": "live",
    "new_cluster" +: {"num_workers": 4}

};

local environments = [testConf, liveConf];

local delete(obj, key) = {
    [k]: obj[k] for k in std.objectFieldsAll(obj) if k != key
};

local streamingJob = {
    jobName: "streaming",
    "main_class_name": "com.databricks.example.streaming.StreamingJob"
};

local devicesJob = {
    jobName: "devices",
    "main_class_name": "com.databricks.example.devices.DevicesJob"
};


local jobs = [streamingJob, devicesJob];

local parameterMap = {
    "test": {
        "streaming": [
            "--source_path", "/databricks-datasets/structured-streaming/events",
            "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/test/data/silver-events",
            "--checkpoint_location", "/mnt/ivan.trusov@databricks.com/examples/test/jsonnet/checkpoints/silver-events",
            "--termination_ms", "10000" # 100 seconds
        ],
        "devices": [
            "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/test/data/silver-devices"
        ],
    },
    "live": {
        "streaming": [
            "--source_path", "/databricks-datasets/structured-streaming/events",
            "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/live/data/silver-events",
            "--checkpoint_location", "/mnt/ivan.trusov@databricks.com/examples/live/jsonnet/checkpoints/silver-events",
        ],
        "devices": [
            "--output_path", "/mnt/ivan.trusov@databricks.com/examples/jsonnet/live/data/silver-devices"
        ]
    }
};

local addParams(env, job) = {
    "main_class_name": job["main_class_name"],
    "parameters": parameterMap[env.envName][job.jobName]
};

local combineEnvAndJob(env, job) = {
    "content": env + {
        "spark_jar_task" +: addParams(env, job),
        "name": "%s-%s-%s-%s-%s" % [userName, env.envName, job.jobName, std.extVar("branch"), std.extVar("version")],
    }
};

local final = {
    ["%s-%s-conf.json" % [job.jobName, env.envName]]: delete(combineEnvAndJob(env,job).content, "envName") for env in environments for job in jobs
};

final