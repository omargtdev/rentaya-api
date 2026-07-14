#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_JAVA_HOME="$PROJECT_DIR/.tools/jdk-17"
LOCAL_COMPOSE="$PROJECT_DIR/.tools/bin/docker-compose"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-$PROJECT_DIR/.tools/m2}"

if [[ -x "$LOCAL_JAVA_HOME/bin/java" ]]; then
  export JAVA_HOME="$LOCAL_JAVA_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "Java 17 is required. Install it or place a JDK in .tools/jdk-17." >&2
  exit 1
fi

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif [[ -x "$LOCAL_COMPOSE" ]]; then
    "$LOCAL_COMPOSE" "$@"
  else
    echo "Docker Compose is required. Install it or place the binary in .tools/bin/docker-compose." >&2
    exit 1
  fi
}

maven() {
  "$PROJECT_DIR/mvnw" -Dmaven.repo.local="$MAVEN_REPO_LOCAL" "$@"
}

cd "$PROJECT_DIR"

case "${1:-run}" in
  db-up)
    compose up -d --wait
    ;;
  db-down)
    compose down
    ;;
  test)
    maven test
    ;;
  build)
    maven clean package
    ;;
  run)
    compose up -d --wait
    export SEED_DATA_ENABLED="${SEED_DATA_ENABLED:-true}"
    maven spring-boot:run
    ;;
  *)
    echo "Usage: $0 {run|test|build|db-up|db-down}" >&2
    exit 1
    ;;
esac
