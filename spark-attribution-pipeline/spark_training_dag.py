from datetime import datetime
from airflow import DAG
from airflow.operators.bash import BashOperator
# Chemin vers le jar compilé par sbt (structure de dossiers propre à sbt 2.x, Scala 2.13)
JAR_PATH = "~/Desktop/data-engineering-portfolio/spark-attribution-pipeline/target/out/jvm/scala-2.13.16/spark-training/spark-training_2.13-0.1.jar"
CSV_PATH = "~/Desktop/data-engineering-portfolio/spark-attribution-pipeline/data/raw/marketing_campaign_performance.csv"

default_args = {
    "owner": "ulrich",
    "retries": 1,
}

with DAG(
        dag_id="spark_training_pipeline",
        description="Pipeline d'entraînement : orchestre le job Spark d'attribution de campagnes",
        default_args=default_args,
        schedule=None,  # déclenchement manuel uniquement, pas de planification automatique
        start_date=datetime(2026, 1, 1),
        catchup=False,
        tags=["training", "spark"],
) as dag:

    run_spark_job = BashOperator(
        task_id="run_campaign_attribution_job",
        bash_command=f"""
        spark-submit \
          --class training.jobs.MarketingPipelineJob \
          --master local[*] \
          {JAR_PATH} \
          {CSV_PATH}
        """,
    )

    run_spark_job