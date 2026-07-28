#!/bin/bash
set -e

# --- Installation d'Airflow dans un environnement virtuel dédié ---
export AIRFLOW_HOME="$HOME/airflow-training"
AIRFLOW_VERSION=2.9.3
PYTHON_VERSION="$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')"
CONSTRAINT_URL="https://raw.githubusercontent.com/apache/airflow/constraints-${AIRFLOW_VERSION}/constraints-${PYTHON_VERSION}.txt"

python3 -m venv "$AIRFLOW_HOME/venv"
source "$AIRFLOW_HOME/venv/bin/activate"

pip install --upgrade pip
pip install "apache-airflow==${AIRFLOW_VERSION}" --constraint "${CONSTRAINT_URL}"

# --- Initialisation de la base (SQLite, suffisant pour un usage local d'entraînement) ---
airflow db migrate

airflow users create \
  --username admin \
  --firstname Ulrich \
  --lastname Training \
  --role Admin \
  --email admin@example.com \
  --password admin

# --- Copie du DAG d'entraînement dans le dossier dags/ d'Airflow ---
mkdir -p "$AIRFLOW_HOME/dags"
cp "$(dirname "$0")/spark_training_dag.py" "$AIRFLOW_HOME/dags/"


