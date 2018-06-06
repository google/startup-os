// Run this to produce a clang JSON from a bazel extra action with CppAction.
//   https://docs.bazel.build/versions/master/be/extra-actions.html
//   https://clang.llvm.org/docs/JSONCompilationDatabase.html
// Bazel extra actions are experimental, and the CppAction is currently a native
// action, while native actions are expected to be replaced with skylark actions
// some day.
//
// Clang JSON compilation database is a way to tell tools that operate on C++
// how what flags and paths they need in order to parse it.

#include <fstream>
#include <string>
#include <cstdlib>

#include <unistd.h>

#include "google/protobuf/io/coded_stream.h"
#include "google/protobuf/io/zero_copy_stream_impl.h"
#include "google/protobuf/stubs/common.h"

#include "src/main/protobuf/extra_actions_base.pb.h"

#include "include/json/json.h"

// Convert a blaze::ExtraActionInfo that contains a blaze::CppCompileInfo into
// a clang JSON compilation database.
int main(int argc, char **argv) {
  if (argc != 3) {
    std::cerr << "usage: extra_action_file.proto compile_commands.json\n";
    std::abort();
  }

  blaze::ExtraActionInfo info;
  blaze::CppCompileInfo cpp_info;

  std::ifstream extra_action_file{argv[1]};
  google::protobuf::io::IstreamInputStream extra_action_istream{
      &extra_action_file};
  google::protobuf::io::CodedInputStream extra_action_coded{
      &extra_action_istream};

  info.ParseFromCodedStream(&extra_action_coded);
  if (!info.HasExtension(blaze::CppCompileInfo::cpp_compile_info)) {
    std::cerr << "action has no CppCompileInfo!";
    std::abort();
  }
  cpp_info = info.GetExtension(blaze::CppCompileInfo::cpp_compile_info);

  Json::Value root;

  // The source path is relative to the compilation sandbox. The sandbox
  // includes other files that the compilation of this file would depend on
  // (such as header files), but does not contain the rest of the source tree.
  root["file"] = cpp_info.source_file();

  // Name of the output that bazel is running this action to produce. For
  // instance, a source file may be used to generate a module file and a .o
  // file.
  root["output"] = cpp_info.output_file();

  // Path to the current sandbox. This path is not valid after the extra action
  // completes.

  #ifdef _GNU_SOURCE
  root["directory"] = get_current_dir_name();
  #else
  size_t directory_buffer_size = sizeof(char) * 1024;
  char *directory_buffer = (char*) malloc(directory_buffer_size);
  getcwd(directory_buffer, directory_buffer_size);
  root["directory"] = directory_buffer;
  #endif

  Json::Value arguments;
  arguments.resize(cpp_info.compiler_option_size());
  for (auto option : cpp_info.compiler_option()) {
    arguments.append(option);
  }
  root["arguments"] = arguments;

  std::ofstream compilation_database_file{argv[2]};
  compilation_database_file << root;

  return 0;
}
