import json
import logging
import os
import time
from argparse import ArgumentParser
from typing import Dict, Any

from databricks_cli.dbfs.api import DbfsApi
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
    p.add_argument("--trace", action='store_true')
    p.add_argument("--run-now", action='store_true')
    return p


def deploy(client: ApiClient, job_conf: Dict[str, Any], task_args: Dict[str, Any]):
    dbfs_new_jar_name = job_conf['libraries'][0]['jar']
    logging.info("Submitting job with configuration %s and jar file %s" % (job_conf, dbfs_new_jar_name))

    dbfs_api = DbfsApi(client)

    dbfs_api.cp(recursive=False, overwrite=True, src=task_args["jar"], dst=dbfs_new_jar_name)

    job_data = client.perform_query('POST', '/jobs/create', data=job_conf, headers=None)

    logging.info("Job creation data %s" % job_data)

    if task_args["run_now"]:
        logging.info("Requested to launch job immediately")
        run_data = client.perform_query('POST', '/jobs/run-now', data=job_data, headers=None)
        logging.info("Job launched with run data: %s" % run_data)
        if task_args["trace"]:
            logging.info("Requested to trace the job status")
            run_finised = False
            while not run_finised:
                time.sleep(4)
                run_status = client.perform_query('GET', '/jobs/runs/get',
                                                  data={"run_id": run_data["run_id"]},
                                                  headers=None)
                logging.info(run_status)
                result_state = run_status["state"].get("result_state", None)
                if result_state:
                    run_finised = True
                    if result_state == "SUCCESS":
                        logging.info("Job successfully finished!")
                    else:
                        exception_text = "Job finished with result state %s. Please check run UI at %s" % (
                            result_state,
                            run_status["run_page_url"]
                        )
                        raise Exception(exception_text)
    logging.info("All deployment actions successfully performed")


def load_conf(json_file: str) -> Dict[str, Any]:
    with open(os.path.join(os.getcwd(), json_file), "r") as reader:
        return json.load(reader)


if __name__ == '__main__':
    parser = get_parser()
    parsed_args = parser.parse_args().__dict__
    logging.info("Provided arguments %s" % parsed_args)

    job_configuration = load_conf(parsed_args["json_file"])
    db_client = ApiClient(host=DB_HOST, token=DB_TOKEN)

    deploy(db_client, job_configuration, parsed_args)
