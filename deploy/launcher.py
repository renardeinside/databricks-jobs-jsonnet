import json
import logging
import os
from argparse import ArgumentParser
from typing import Dict, Any

from databricks_cli.runs.api import RunsApi
from databricks_cli.sdk.api_client import ApiClient

FORMAT = u'[%(asctime)s] %(levelname)s %(message)s'
logging.basicConfig(format=FORMAT, level=logging.INFO)

DB_HOST = os.environ['DATABRICKS_HOST']
DB_TOKEN = os.environ['DATABRICKS_TOKEN']


def get_parser():
    p = ArgumentParser(description="Simple and programmable Databricks jobs launcher")
    p.add_argument("--branch", type=str, required=True)
    p.add_argument("--build-number", type=str, required=True)
    p.add_argument("--json-file", type=str, required=True)
    p.add_argument("--trace", type=bool)
    return p


def submit(client: ApiClient, job_conf: Dict[str, Any], trace: bool):
    logging.info("Submitting job with configuration %s" % job_conf)
    runs_api = RunsApi(client)
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
    submit(db_client, job_configuration, parsed_args["trace"])
