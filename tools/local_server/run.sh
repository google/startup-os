# Run LocalHttpGateway and LocalServer
# Usage:
# run.sh --root_path <root_path>

# Build and exit on fail:
bazel build //tools/local_server:local_http_gateway
if [ $? -ne 0 ]; then
  exit $?
fi
bazel build //tools/local_server:local_server
if [ $? -ne 0 ]; then
  exit $?
fi

# Kill previous:
pkill -f 'tools/local_server/local_http_gateway'
pkill -f 'tools/local_server/local_server'
sleep 0.5  # Wait for processes to terminate

# Run:
bazel-bin/tools/local_server/local_http_gateway &
bazel-bin/tools/local_server/local_server "$@"
