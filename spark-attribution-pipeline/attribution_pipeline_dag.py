from datetime import datetime
from airflow import DAG
from airflow.operators.bash import BashOperator

# Chemin vers le jar compilé par sbt (structure de dossiers propre à sbt 2.x, Scala 2.13)
JAR_PATH = "~/Desktop/data-engineering-portfolio/spark-attribution-pipeline/target/out/jvm/scala-2.13.16/spark-training/spark-training_2.13-0.1.jar"

default_args = {
    "owner": "ulrich",
    "retries": 1,
}

with DAG(
        dag_id="attribution_pipeline",
        description="Pipeline d'attribution multi-sources (clicks/visits/campaigns) avec skew et fenêtre d'attribution",
        default_args=default_args,
        schedule=None,  # déclenchement manuel uniquement
        start_date=datetime(2026, 1, 1),
        catchup=False,
        tags=["training", "spark", "attribution"],
) as dag:

    run_attribution_job = BashOperator(
        task_id="run_attribution_pipeline_job",
        bash_command=f"""
        spark-submit \
          --class training.jobs.AttributionPipelineJob \
          --master local[*] \
          {JAR_PATH}
        """,
    )

    run_attribution_job