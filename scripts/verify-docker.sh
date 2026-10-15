#!/usr/bin/env bash
# Run every test against a disposable, loopback-only Elasticsearch instance.
set -euo pipefail

repo_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
cd "$repo_dir"
maven_bin=${MVN:-mvn}
image=${ES_IMAGE:-docker.elastic.co/elasticsearch/elasticsearch:8.2.2}
startup_timeout=${ES_STARTUP_TIMEOUT:-120}
case "$startup_timeout" in ''|*[!0-9]*|0*) echo 'ES_STARTUP_TIMEOUT must be a positive number of seconds without leading zeros' >&2; exit 2;; esac
for executable in docker curl "$maven_bin"; do
    command -v "$executable" >/dev/null || { echo "Missing executable: $executable" >&2; exit 2; }
done
docker info >/dev/null
mkdir -p target
work_dir=$(mktemp -d)
log_file="$repo_dir/target/elasticsearch-integration.log"

cleanup() {
    result=$?
    trap - EXIT
    if [[ -s "$work_dir/container.cid" ]]; then
        container_id=$(cat "$work_dir/container.cid")
        docker logs "$container_id" >"$log_file" 2>&1 || true
        if [[ $result -ne 0 ]]; then tail -n 80 "$log_file" >&2; fi
        if ! docker rm -f "$container_id" >/dev/null; then
            echo "Could not remove test container $container_id" >&2
            if [[ $result -eq 0 ]]; then result=1; fi
        fi
    fi
    rm -rf -- "$work_dir"
    exit "$result"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

echo "Starting disposable Elasticsearch: $image"
# The 8.2.2 image's old JVM crashes on some modern cgroup hosts. Apply this to
# CLI JVMs too; heap size and Docker's memory limit remain explicitly bounded.
docker run -d --cidfile "$work_dir/container.cid" --memory=2g \
    -p 127.0.0.1::9200 \
    -e discovery.type=single-node -e xpack.security.enabled=false \
    -e ES_JAVA_OPTS='-Xms512m -Xmx512m'     -e JAVA_TOOL_OPTIONS='-XX:-UseContainerSupport' "$image" >/dev/null
container_id=$(cat "$work_dir/container.cid")
address=$(docker port "$container_id" 9200/tcp)
endpoint="http://$address"
deadline=$((SECONDS + startup_timeout))
ready=false
while (( SECONDS < deadline )); do
    if [[ $(docker inspect --format '{{.State.Running}}' "$container_id") != true ]]; then
        echo 'Elasticsearch exited during startup' >&2
        exit 1
    fi
    health=$(curl --silent --fail --max-time 3 "$endpoint/_cluster/health?wait_for_status=yellow&timeout=1s" || true)
    if echo "$health" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"(yellow|green)"'; then
        ready=true
        break
    fi
    sleep 1
done
if [[ $ready != true ]]; then
    echo "Elasticsearch did not become ready within $startup_timeout seconds" >&2
    exit 1
fi

echo "Running the full suite against $endpoint"
"$maven_bin" --batch-mode test "$@" "-Delasticstore.test.url=$endpoint"
echo "Elasticsearch log: $log_file"
