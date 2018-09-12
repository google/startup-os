# Run LocalHttpGateway and LocalServer
# Usage:
# run.sh --root_path <root_path>

#
# Debugging gRPC server:
# kill $(lsof -tnP -i:8001 -sTCP:LISTEN)
# bazel build //tools/reviewer/local_server:local_server; bazel-bin/tools/reviewer/local_server/local_server

cd $STARTUP_OS
# Build and exit on fail:
bazel build //tools/reviewer/local_server:local_http_gateway
if [ $? -ne 0 ]; then
  exit $?
fi
bazel build //tools/reviewer/local_server:local_server
if [ $? -ne 0 ]; then
  exit $?
fi

# Kill previous:
if [ "$(uname)" = "Darwin" ] || [ "$(uname)" = "Linux" ]; then
  # Mac & Linux
  # kill HTTP gateway (port 7000), gRPC server (port 8001) and Angular server (8000)
  # -t makes `lsof` output only PIDs so output can be piped to `kill`
  # -n and -P prevent `lsof` from resolving addresses and ports, therefore
  # making execution faster
  # we filter for processes that listen on port in order to kill only
  # servers (i.e. Angular) instead of clients (i.e. Chrome)
  kill $(lsof -tnP -i:7000 -i:8000 -i:8001 -sTCP:LISTEN)
# else
#   # Windows
#   # TODO: kill local_http_gateway & local_server
fi

cd tools/reviewer/local_server/web_login
npm install
npm run serve &
cd -

# Run:
bazel-bin/tools/reviewer/local_server/local_http_gateway &
bazel-bin/tools/reviewer/local_server/local_server "$@"
