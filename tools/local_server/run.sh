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
if [ "$(uname)" = "Darwin" ] || [ "$(uname)" = "Linux" ]; then
  # Mac & Linux
  pkill -f 'tools/local_server/local_http_gateway'
  pkill -f 'tools/local_server/local_server'
  kill $(lsof -t -i:8000) # For Angular
# else
#   # Windows
#   # TODO: kill local_http_gateway & local_server
fi

cd tools/local_server/web_login
npm install
ng serve &
cd -

# Run:
bazel-bin/tools/local_server/local_http_gateway &
bazel-bin/tools/local_server/local_server "$@"
