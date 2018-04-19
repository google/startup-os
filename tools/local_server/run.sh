# Run LocalHttpGateway and LocalServer
# Usage:
# run.sh <root_path>

# Build and exit on fail:
bazel build //tools/local_server:local_http_gateway_deploy.jar
if [ $? -ne 0 ]; then
  exit $?
fi
bazel build //tools/local_server:local_server_deploy.jar
if [ $? -ne 0 ]; then
  exit $?
fi

# Kill previous:
pkill -f 'tools/local_server/local_http_gateway'
pkill -f 'tools/local_server/local_server'
sleep 0.5  # Wait for processes to terminate

# Run:
java -jar bazel-bin/tools/local_server/local_http_gateway_deploy.jar &
java -jar bazel-bin/tools/local_server/local_server_deploy.jar --root_path $1