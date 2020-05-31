import json
import logging
import os
from argparse import ArgumentParser
from typing import Dict, Any

from databricks_cli.dbfs.api import DbfsApi
from databricks_cli.runs.api import RunsApi
from databricks_cli.sdk.api_client import ApiClient

FORMAT = u'[%(asctime)s] %(levelname)s %(message)s'
logging.basicConfig(format=FORMAT, level=logging.INFO)

DB_HOST = os.environ['DATABRICKS_HOST']
DB_TOKEN = os.environ['DATABRICKS_TOKEN']

BRANCH_NAME = os.environ['BRANCH_NAME']
BUILD_NUMBER = os.environ['BUILD_NUMBER']


def get_parser():
    p = ArgumentParser(description="Simple and programmable Databricks jobs launcher")
    p.add_argument("--jar", type=str, required=True)
    p.add_argument("--json-file", type=str, required=True)
    p.add_argument("--trace", type=bool, action='store_true')
    return p


def deploy(client: ApiClient, jar: str, job_conf: Dict[str, Any], trace: bool):
    dbfs_new_jar_name = job_conf['libraries']['jar']
    logging.info("Submitting job with configuration %s and jar file %s" % (job_conf, dbfs_new_jar_name))

    dbfs_api = DbfsApi(client)
    runs_api = RunsApi(client)

    dbfs_api.cp(recursive=False, overwrite=True, src=jar, dst=dbfs_new_jar_name)

    run_id = runs_api.submit_run(job_conf)
    logging.info("Job submitted with run id: %s" % run_id)
    if trace:
        logging.info("Tracing submitted job by run id")


def load_conf(json_file: str) -> Dict[str, Any]:
    with open(json_file, "r") as reader:
        return json.load(reader)


if __name__ == '__main__':
    parser = get_parser()
    parsed_args = parser.parse_args().__dict__
    logging.info("Provided arguments %s" % parsed_args)

    job_configuration = load_conf(parsed_args["json_file"])
    db_client = ApiClient(host=DB_HOST, token=DB_TOKEN)

    deploy(db_client, parsed_args["jar"], job_configuration, parsed_args["trace"])
