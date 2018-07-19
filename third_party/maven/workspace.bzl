# Do not edit. bazel-deps autogenerates this file from dependencies.yaml.

def declare_maven(hash):
    native.maven_jar(
        name = hash["name"],
        artifact = hash["artifact"],
        sha1 = hash["sha1"],
        repository = hash["repository"]
    )
    native.bind(
        name = hash["bind"],
        actual = hash["actual"]
    )

def list_dependencies():
    return [
    {"artifact": "com.fasterxml.jackson.core:jackson-core:2.1.3", "lang": "java", "sha1": "f6c3aed1cdfa21b5c1737c915186ea93a95a58bd", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_fasterxml_jackson_core_jackson_core", "actual": "@com_fasterxml_jackson_core_jackson_core//jar", "bind": "jar/com/fasterxml/jackson/core/jackson_core"},
    {"artifact": "com.google.api-client:google-api-client-gson:1.23.0", "lang": "java", "sha1": "37d56e99d5383fc1ae2a8d9877df6557df19682b", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_client_google_api_client_gson", "actual": "@com_google_api_client_google_api_client_gson//jar", "bind": "jar/com/google/api_client/google_api_client_gson"},
    {"artifact": "com.google.api-client:google-api-client:1.23.0", "lang": "java", "sha1": "522ea860eb48dee71dfe2c61a1fd09663539f556", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_client_google_api_client", "actual": "@com_google_api_client_google_api_client//jar", "bind": "jar/com/google/api_client/google_api_client"},
    {"artifact": "com.google.api.grpc:proto-google-cloud-firestore-v1beta1:0.1.28", "lang": "java", "sha1": "bf1a882693a239cc7d40bb7f162150a055a5f28a", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_grpc_proto_google_cloud_firestore_v1beta1", "actual": "@com_google_api_grpc_proto_google_cloud_firestore_v1beta1//jar", "bind": "jar/com/google/api/grpc/proto_google_cloud_firestore_v1beta1"},
# duplicates in com.google.api.grpc:proto-google-common-protos promoted to 1.0.4
# - com.google.cloud:google-cloud-core:1.15.0 wanted version 1.0.4
# - io.grpc:grpc-protobuf:1.12.0 wanted version 1.0.0
    {"artifact": "com.google.api.grpc:proto-google-common-protos:1.0.4", "lang": "java", "sha1": "771c6220544afe0169bec7539efa527fd859d4ed", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_grpc_proto_google_common_protos", "actual": "@com_google_api_grpc_proto_google_common_protos//jar", "bind": "jar/com/google/api/grpc/proto_google_common_protos"},
    {"artifact": "com.google.api.grpc:proto-google-iam-v1:0.1.28", "lang": "java", "sha1": "c9e7cd603fc43814656611d54aca71305c13b17d", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_grpc_proto_google_iam_v1", "actual": "@com_google_api_grpc_proto_google_iam_v1//jar", "bind": "jar/com/google/api/grpc/proto_google_iam_v1"},
    {"artifact": "com.google.api:api-common:1.1.0", "lang": "java", "sha1": "14733901500ad0744cebf7adf73045a466ce1a11", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_api_common", "actual": "@com_google_api_api_common//jar", "bind": "jar/com/google/api/api_common"},
    {"artifact": "com.google.api:gax-grpc:1.16.0", "lang": "java", "sha1": "ece0460bada7d27918149c455f874871dd4144ed", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_gax_grpc", "actual": "@com_google_api_gax_grpc//jar", "bind": "jar/com/google/api/gax_grpc"},
    {"artifact": "com.google.api:gax-httpjson:0.33.0", "lang": "java", "sha1": "03b48977bfa3b31c4f856774257d22931bd7035a", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_gax_httpjson", "actual": "@com_google_api_gax_httpjson//jar", "bind": "jar/com/google/api/gax_httpjson"},
    {"artifact": "com.google.api:gax:1.16.0", "lang": "java", "sha1": "e20231eb08fa520b9a5e511e1104dfa8656f5631", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_api_gax", "actual": "@com_google_api_gax//jar", "bind": "jar/com/google/api/gax"},
    {"artifact": "com.google.apis:google-api-services-storage:v1-rev114-1.23.0", "lang": "java", "sha1": "53c10dbf25674f3877a2784b219763b75fe484be", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_apis_google_api_services_storage", "actual": "@com_google_apis_google_api_services_storage//jar", "bind": "jar/com/google/apis/google_api_services_storage"},
    {"artifact": "com.google.auth:google-auth-library-credentials:0.8.0", "lang": "java", "sha1": "0585c4d65c6788ca0414e9a0434482094ac80c5a", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_auth_google_auth_library_credentials", "actual": "@com_google_auth_google_auth_library_credentials//jar", "bind": "jar/com/google/auth/google_auth_library_credentials"},
    {"artifact": "com.google.auth:google-auth-library-oauth2-http:0.8.0", "lang": "java", "sha1": "03b343c91d25d1bdae085f4f491b921a94cea05e", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_auth_google_auth_library_oauth2_http", "actual": "@com_google_auth_google_auth_library_oauth2_http//jar", "bind": "jar/com/google/auth/google_auth_library_oauth2_http"},
    {"artifact": "com.google.auto.factory:auto-factory:1.0-beta5", "lang": "java", "sha1": "78b93b2334d0e2fb0d65c00127d4dcce261a83fc", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_auto_factory_auto_factory", "actual": "@com_google_auto_factory_auto_factory//jar", "bind": "jar/com/google/auto/factory/auto_factory"},
# duplicates in com.google.auto.value:auto-value fixed to 1.5
# - com.google.api:gax-grpc:1.16.0 wanted version 1.2
# - com.google.auto.factory:auto-factory:1.0-beta5 wanted version 1.1
    {"artifact": "com.google.auto.value:auto-value:1.5", "lang": "java", "sha1": "ed31b6bc2e3723c26ea86439862d12ad311b64b3", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_auto_value_auto_value", "actual": "@com_google_auto_value_auto_value//jar", "bind": "jar/com/google/auto/value/auto_value"},
    {"artifact": "com.google.auto:auto-common:0.6", "lang": "java", "sha1": "cf7212b0f8bfef12657b942df8f4f2cf032d3f41", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_auto_auto_common", "actual": "@com_google_auto_auto_common//jar", "bind": "jar/com/google/auto/auto_common"},
    {"artifact": "com.google.cloud:google-cloud-core-grpc:1.15.0", "lang": "java", "sha1": "6f17a237b69bed4f87bcac440164ba6e740c32c5", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_cloud_google_cloud_core_grpc", "actual": "@com_google_cloud_google_cloud_core_grpc//jar", "bind": "jar/com/google/cloud/google_cloud_core_grpc"},
    {"artifact": "com.google.cloud:google-cloud-core-http:1.15.0", "lang": "java", "sha1": "0ffe79ff309af26c6eeeaa8f52dc24ce797805b4", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_cloud_google_cloud_core_http", "actual": "@com_google_cloud_google_cloud_core_http//jar", "bind": "jar/com/google/cloud/google_cloud_core_http"},
    {"artifact": "com.google.cloud:google-cloud-core:1.15.0", "lang": "java", "sha1": "c0810f30f96d6f94335cdad7bf17009c466dcbe9", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_cloud_google_cloud_core", "actual": "@com_google_cloud_google_cloud_core//jar", "bind": "jar/com/google/cloud/google_cloud_core"},
    {"artifact": "com.google.cloud:google-cloud-firestore:0.33.0-beta", "lang": "java", "sha1": "70eea83c30e7472f7dbdbcf12f28a3fc4a34f4f3", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_cloud_google_cloud_firestore", "actual": "@com_google_cloud_google_cloud_firestore//jar", "bind": "jar/com/google/cloud/google_cloud_firestore"},
    {"artifact": "com.google.cloud:google-cloud-storage:1.15.0", "lang": "java", "sha1": "b2c6b1361eef9b084f9a5bfabf350b4997fb087b", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_cloud_google_cloud_storage", "actual": "@com_google_cloud_google_cloud_storage//jar", "bind": "jar/com/google/cloud/google_cloud_storage"},
# duplicates in com.google.code.findbugs:jsr305 fixed to 3.0.2
# - com.google.api:api-common:1.1.0 wanted version 3.0.0
# - com.google.flogger:flogger-system-backend:0.1 wanted version 3.0.1
# - com.google.flogger:flogger:0.1 wanted version 3.0.1
# - com.google.guava:guava:24.0-android wanted version 1.3.9
# - com.google.http-client:google-http-client:1.23.0 wanted version 1.3.9
# - com.google.instrumentation:instrumentation-api:0.4.3 wanted version 3.0.0
# - io.grpc:grpc-core:1.12.0 wanted version 3.0.0
    {"artifact": "com.google.code.findbugs:jsr305:3.0.2", "lang": "java", "sha1": "25ea2e8b0c338a877313bd4672d3fe056ea78f0d", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_code_findbugs_jsr305", "actual": "@com_google_code_findbugs_jsr305//jar", "bind": "jar/com/google/code/findbugs/jsr305"},
# duplicates in com.google.code.gson:gson fixed to 2.8.2
# - com.google.http-client:google-http-client-gson:1.23.0 wanted version 2.1
# - com.google.protobuf:protobuf-java-util:3.6.0 wanted version 2.7
# - io.grpc:grpc-core:1.12.0 wanted version 2.7
    {"artifact": "com.google.code.gson:gson:2.8.2", "lang": "java", "sha1": "3edcfe49d2c6053a70a2a47e4e1c2f94998a49cf", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_code_gson_gson", "actual": "@com_google_code_gson_gson//jar", "bind": "jar/com/google/code/gson/gson"},
    {"artifact": "com.google.dagger:dagger-compiler:2.15", "lang": "java", "sha1": "c114d70b9a4e814132fc147e8b79b1a294e5c2db", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_dagger_dagger_compiler", "actual": "@com_google_dagger_dagger_compiler//jar", "bind": "jar/com/google/dagger/dagger_compiler"},
    {"artifact": "com.google.dagger:dagger-producers:2.15", "lang": "java", "sha1": "cb9261e680cf2e0a9e1f0693f92b671781cb9f96", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_dagger_dagger_producers", "actual": "@com_google_dagger_dagger_producers//jar", "bind": "jar/com/google/dagger/dagger_producers"},
    {"artifact": "com.google.dagger:dagger-spi:2.15", "lang": "java", "sha1": "b2a7a008233795b6a279c901485b0269549972d6", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_dagger_dagger_spi", "actual": "@com_google_dagger_dagger_spi//jar", "bind": "jar/com/google/dagger/dagger_spi"},
    {"artifact": "com.google.dagger:dagger:2.15", "lang": "java", "sha1": "13cc1f509deda05c1fe5a315519d7cb743b8333b", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_dagger_dagger", "actual": "@com_google_dagger_dagger//jar", "bind": "jar/com/google/dagger/dagger"},
# duplicates in com.google.errorprone:error_prone_annotations promoted to 2.1.3
# - com.google.guava:guava:24.0-android wanted version 2.1.3
# - io.grpc:grpc-core:1.12.0 wanted version 2.1.2
    {"artifact": "com.google.errorprone:error_prone_annotations:2.1.3", "lang": "java", "sha1": "39b109f2cd352b2d71b52a3b5a1a9850e1dc304b", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_errorprone_error_prone_annotations", "actual": "@com_google_errorprone_error_prone_annotations//jar", "bind": "jar/com/google/errorprone/error_prone_annotations"},
    {"artifact": "com.google.errorprone:javac-shaded:9-dev-r4023-3", "lang": "java", "sha1": "72b688efd290280a0afde5f9892b0fde6f362d1d", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_errorprone_javac_shaded", "actual": "@com_google_errorprone_javac_shaded//jar", "bind": "jar/com/google/errorprone/javac_shaded"},
    {"artifact": "com.google.firebase:firebase-admin:5.9.0", "lang": "java", "sha1": "c8e8c4a191158b3fb711c757cb48ed2f9ba239f9", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_firebase_firebase_admin", "actual": "@com_google_firebase_firebase_admin//jar", "bind": "jar/com/google/firebase/firebase_admin"},
    {"artifact": "com.google.flogger:flogger-system-backend:0.1", "lang": "java", "sha1": "051278e0c81e2eaf5e275e4275a8fb9ca5967695", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_flogger_flogger_system_backend", "actual": "@com_google_flogger_flogger_system_backend//jar", "bind": "jar/com/google/flogger/flogger_system_backend"},
    {"artifact": "com.google.flogger:flogger:0.1", "lang": "java", "sha1": "e02b7e9c66921e31b506416ef690c72774e1ed65", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_flogger_flogger", "actual": "@com_google_flogger_flogger//jar", "bind": "jar/com/google/flogger/flogger"},
# duplicates in com.google.googlejavaformat:google-java-format fixed to 1.5
# - com.google.auto.factory:auto-factory:1.0-beta5 wanted version 1.1
# - com.google.dagger:dagger-compiler:2.15 wanted version 1.4
    {"artifact": "com.google.googlejavaformat:google-java-format:1.5", "lang": "java", "sha1": "fba7f130d29061d2d2ea384b4880c10cae92ef73", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_googlejavaformat_google_java_format", "actual": "@com_google_googlejavaformat_google_java_format//jar", "bind": "jar/com/google/googlejavaformat/google_java_format"},
# duplicates in com.google.guava:guava fixed to 24.0-android
# - com.google.api:api-common:1.1.0 wanted version 19.0
# - com.google.auto.factory:auto-factory:1.0-beta5 wanted version 19.0
# - com.google.auto:auto-common:0.6 wanted version 18.0
# - com.google.dagger:dagger-compiler:2.15 wanted version 23.3-jre
# - com.google.firebase:firebase-admin:5.9.0 wanted version 20.0
# - com.google.googlejavaformat:google-java-format:1.5 wanted version 22.0
# - com.google.jimfs:jimfs:1.1 wanted version 18.0
# - com.google.protobuf:protobuf-java-util:3.6.0 wanted version 19.0
# - io.grpc:grpc-core:1.12.0 wanted version 20.0
# - io.grpc:grpc-protobuf:1.12.0 wanted version 20.0
# - io.opencensus:opencensus-api:0.15.0 wanted version 20.0
    {"artifact": "com.google.guava:guava:24.0-android", "lang": "java", "sha1": "bfc941bd9285e7534ebde47236f14e5c7396a90c", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_guava_guava", "actual": "@com_google_guava_guava//jar", "bind": "jar/com/google/guava/guava"},
    {"artifact": "com.google.http-client:google-http-client-appengine:1.23.0", "lang": "java", "sha1": "0eda0d0f758c1cc525866e52e1226c4eb579d130", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_http_client_google_http_client_appengine", "actual": "@com_google_http_client_google_http_client_appengine//jar", "bind": "jar/com/google/http_client/google_http_client_appengine"},
    {"artifact": "com.google.http-client:google-http-client-gson:1.23.0", "lang": "java", "sha1": "7029b196174e7f424217d047a9d1966dd2aa61df", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_http_client_google_http_client_gson", "actual": "@com_google_http_client_google_http_client_gson//jar", "bind": "jar/com/google/http_client/google_http_client_gson"},
    {"artifact": "com.google.http-client:google-http-client-jackson2:1.23.0", "lang": "java", "sha1": "fd6761f4046a8cb0455e6fa5f58e12b061e9826e", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_http_client_google_http_client_jackson2", "actual": "@com_google_http_client_google_http_client_jackson2//jar", "bind": "jar/com/google/http_client/google_http_client_jackson2"},
    {"artifact": "com.google.http-client:google-http-client-jackson:1.23.0", "lang": "java", "sha1": "a72ea3a197937ef63a893e73df312dac0d813663", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_http_client_google_http_client_jackson", "actual": "@com_google_http_client_google_http_client_jackson//jar", "bind": "jar/com/google/http_client/google_http_client_jackson"},
    {"artifact": "com.google.http-client:google-http-client:1.23.0", "lang": "java", "sha1": "8e86c84ff3c98eca6423e97780325b299133d858", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_http_client_google_http_client", "actual": "@com_google_http_client_google_http_client//jar", "bind": "jar/com/google/http_client/google_http_client"},
    {"artifact": "com.google.instrumentation:instrumentation-api:0.4.3", "lang": "java", "sha1": "41614af3429573dc02645d541638929d877945a2", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_instrumentation_instrumentation_api", "actual": "@com_google_instrumentation_instrumentation_api//jar", "bind": "jar/com/google/instrumentation/instrumentation_api"},
    {"artifact": "com.google.j2objc:j2objc-annotations:1.1", "lang": "java", "sha1": "ed28ded51a8b1c6b112568def5f4b455e6809019", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_j2objc_j2objc_annotations", "actual": "@com_google_j2objc_j2objc_annotations//jar", "bind": "jar/com/google/j2objc/j2objc_annotations"},
    {"artifact": "com.google.jimfs:jimfs:1.1", "lang": "java", "sha1": "8fbd0579dc68aba6186935cc1bee21d2f3e7ec1c", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_jimfs_jimfs", "actual": "@com_google_jimfs_jimfs//jar", "bind": "jar/com/google/jimfs/jimfs"},
    {"artifact": "com.google.oauth-client:google-oauth-client:1.23.0", "lang": "java", "sha1": "e57ea1e2220bda5a2bd24ff17860212861f3c5cf", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_oauth_client_google_oauth_client", "actual": "@com_google_oauth_client_google_oauth_client//jar", "bind": "jar/com/google/oauth_client/google_oauth_client"},
    {"artifact": "com.google.protobuf:protobuf-java-util:3.6.0", "lang": "java", "sha1": "3680d0042d4fe0b95ada844ff24da0698a7f0773", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_protobuf_protobuf_java_util", "actual": "@com_google_protobuf_protobuf_java_util//jar", "bind": "jar/com/google/protobuf/protobuf_java_util"},
# duplicates in com.google.protobuf:protobuf-java fixed to 3.6.0
# - com.google.cloud:google-cloud-core-grpc:1.15.0 wanted version 3.5.1
# - com.google.protobuf:protobuf-java-util:3.6.0 wanted version 3.6.0
# - io.grpc:grpc-protobuf:1.12.0 wanted version 3.5.1
    {"artifact": "com.google.protobuf:protobuf-java:3.6.0", "lang": "java", "sha1": "5333f7e422744d76840c08a106e28e519fbe3acd", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_google_protobuf_protobuf_java", "actual": "@com_google_protobuf_protobuf_java//jar", "bind": "jar/com/google/protobuf/protobuf_java"},
    {"artifact": "com.googlecode.javaewah:JavaEWAH:1.1.6", "lang": "java", "sha1": "94ad16d728b374d65bd897625f3fbb3da223a2b6", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_googlecode_javaewah_JavaEWAH", "actual": "@com_googlecode_javaewah_JavaEWAH//jar", "bind": "jar/com/googlecode/javaewah/JavaEWAH"},
    {"artifact": "com.jcraft:jsch:0.1.54", "lang": "java", "sha1": "da3584329a263616e277e15462b387addd1b208d", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_jcraft_jsch", "actual": "@com_jcraft_jsch//jar", "bind": "jar/com/jcraft/jsch"},
# duplicates in com.squareup:javapoet promoted to 1.8.0
# - com.google.auto.factory:auto-factory:1.0-beta5 wanted version 1.7.0
# - com.google.dagger:dagger-compiler:2.15 wanted version 1.8.0
    {"artifact": "com.squareup:javapoet:1.8.0", "lang": "java", "sha1": "e858dc62ef484048540d27d36f3ec2177a3fa9b1", "repository": "https://repo.maven.apache.org/maven2/", "name": "com_squareup_javapoet", "actual": "@com_squareup_javapoet//jar", "bind": "jar/com/squareup/javapoet"},
    {"artifact": "commons-codec:commons-codec:1.10", "lang": "java", "sha1": "4b95f4897fa13f2cd904aee711aeafc0c5295cd8", "repository": "https://repo.maven.apache.org/maven2/", "name": "commons_codec_commons_codec", "actual": "@commons_codec_commons_codec//jar", "bind": "jar/commons_codec/commons_codec"},
    {"artifact": "commons-logging:commons-logging:1.2", "lang": "java", "sha1": "4bfc12adfe4842bf07b657f0369c4cb522955686", "repository": "https://repo.maven.apache.org/maven2/", "name": "commons_logging_commons_logging", "actual": "@commons_logging_commons_logging//jar", "bind": "jar/commons_logging/commons_logging"},
    {"artifact": "io.grpc:grpc-auth:1.9.0", "lang": "java", "sha1": "d2eadc6d28ebee8ec0cef74f882255e4069972ad", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_auth", "actual": "@io_grpc_grpc_auth//jar", "bind": "jar/io/grpc/grpc_auth"},
# duplicates in io.grpc:grpc-context promoted to 1.12.0
# - com.google.cloud:google-cloud-core-grpc:1.15.0 wanted version 1.9.0
# - io.grpc:grpc-core:1.12.0 wanted version 1.12.0
# - io.opencensus:opencensus-api:0.15.0 wanted version 1.12.0
    {"artifact": "io.grpc:grpc-context:1.12.0", "lang": "java", "sha1": "5b63a170b786051a42cce08118d5ea3c8f60f749", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_context", "actual": "@io_grpc_grpc_context//jar", "bind": "jar/io/grpc/grpc_context"},
    {"artifact": "io.grpc:grpc-core:1.12.0", "lang": "java", "sha1": "541a5c68ce85c03190e29bc9e0ec611d2b75ff24", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_core", "actual": "@io_grpc_grpc_core//jar", "bind": "jar/io/grpc/grpc_core"},
    {"artifact": "io.grpc:grpc-netty:1.12.0", "lang": "java", "sha1": "a4dfc839dae9206c0d1f8b53657c2a8e4a81ad41", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_netty", "actual": "@io_grpc_grpc_netty//jar", "bind": "jar/io/grpc/grpc_netty"},
    {"artifact": "io.grpc:grpc-protobuf-lite:1.12.0", "lang": "java", "sha1": "f5bebfbd5e93b8bbb58888a5cfaa9f490fb7b455", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_protobuf_lite", "actual": "@io_grpc_grpc_protobuf_lite//jar", "bind": "jar/io/grpc/grpc_protobuf_lite"},
# duplicates in io.grpc:grpc-protobuf fixed to 1.12.0
# - com.google.cloud:google-cloud-core-grpc:1.15.0 wanted version 1.9.0
# - io.grpc:grpc-services:1.12.0 wanted version 1.12.0
    {"artifact": "io.grpc:grpc-protobuf:1.12.0", "lang": "java", "sha1": "fbee015b681b5342e72fe40f88eae9dd6cbce206", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_protobuf", "actual": "@io_grpc_grpc_protobuf//jar", "bind": "jar/io/grpc/grpc_protobuf"},
    {"artifact": "io.grpc:grpc-services:1.12.0", "lang": "java", "sha1": "6af24bde0df8bbaf89fcaef33bf9ebba76c11b83", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_services", "actual": "@io_grpc_grpc_services//jar", "bind": "jar/io/grpc/grpc_services"},
# duplicates in io.grpc:grpc-stub fixed to 1.12.0
# - com.google.cloud:google-cloud-firestore:0.33.0-beta wanted version 1.9.0
# - io.grpc:grpc-services:1.12.0 wanted version 1.12.0
    {"artifact": "io.grpc:grpc-stub:1.12.0", "lang": "java", "sha1": "fbd2bafe09a89442ab3d7a8d8b3e8bafbd59b4e0", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_grpc_grpc_stub", "actual": "@io_grpc_grpc_stub//jar", "bind": "jar/io/grpc/grpc_stub"},
    {"artifact": "io.netty:netty-buffer:4.1.22.Final", "lang": "java", "sha1": "15e964a2095031364f534a6e21977f5ee9ca32a9", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_buffer", "actual": "@io_netty_netty_buffer//jar", "bind": "jar/io/netty/netty_buffer"},
    {"artifact": "io.netty:netty-codec-http2:4.1.22.Final", "lang": "java", "sha1": "6d01daf652551a3219cc07122b765d4c4924dcf8", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_codec_http2", "actual": "@io_netty_netty_codec_http2//jar", "bind": "jar/io/netty/netty_codec_http2"},
# duplicates in io.netty:netty-codec-http promoted to 4.1.22.Final
# - com.google.firebase:firebase-admin:5.9.0 wanted version 4.1.17.Final
# - io.netty:netty-codec-http2:4.1.22.Final wanted version 4.1.22.Final
    {"artifact": "io.netty:netty-codec-http:4.1.22.Final", "lang": "java", "sha1": "3805f3ca0d57630200defc7f9bb6ed3382dcb10b", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_codec_http", "actual": "@io_netty_netty_codec_http//jar", "bind": "jar/io/netty/netty_codec_http"},
    {"artifact": "io.netty:netty-codec-socks:4.1.22.Final", "lang": "java", "sha1": "d077b39da2dedc5dc5db50a44e5f4c30353e86f3", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_codec_socks", "actual": "@io_netty_netty_codec_socks//jar", "bind": "jar/io/netty/netty_codec_socks"},
    {"artifact": "io.netty:netty-codec:4.1.22.Final", "lang": "java", "sha1": "239c0af275952e70bb4adf7cf8c03d88ddc394c9", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_codec", "actual": "@io_netty_netty_codec//jar", "bind": "jar/io/netty/netty_codec"},
    {"artifact": "io.netty:netty-common:4.1.22.Final", "lang": "java", "sha1": "56ff4deca53fc791ed59ac2b72eb6718714a4de9", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_common", "actual": "@io_netty_netty_common//jar", "bind": "jar/io/netty/netty_common"},
    {"artifact": "io.netty:netty-handler-proxy:4.1.22.Final", "lang": "java", "sha1": "8eabe24f0b8e95d0873964666ad070179ca81e72", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_handler_proxy", "actual": "@io_netty_netty_handler_proxy//jar", "bind": "jar/io/netty/netty_handler_proxy"},
# duplicates in io.netty:netty-handler promoted to 4.1.22.Final
# - com.google.firebase:firebase-admin:5.9.0 wanted version 4.1.17.Final
# - io.netty:netty-codec-http2:4.1.22.Final wanted version 4.1.22.Final
    {"artifact": "io.netty:netty-handler:4.1.22.Final", "lang": "java", "sha1": "a3a16b17d5a5ed6f784b0daba95e28d940356109", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_handler", "actual": "@io_netty_netty_handler//jar", "bind": "jar/io/netty/netty_handler"},
    {"artifact": "io.netty:netty-resolver:4.1.22.Final", "lang": "java", "sha1": "b5484d17a97cb57b07d2a1ac092c249e47234c17", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_resolver", "actual": "@io_netty_netty_resolver//jar", "bind": "jar/io/netty/netty_resolver"},
    {"artifact": "io.netty:netty-tcnative-boringssl-static:2.0.7.Final", "lang": "java", "sha1": "a8ec0f0ee612fa89c709bdd3881c3f79fa00431d", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_tcnative_boringssl_static", "actual": "@io_netty_netty_tcnative_boringssl_static//jar", "bind": "jar/io/netty/netty_tcnative_boringssl_static"},
# duplicates in io.netty:netty-transport promoted to 4.1.22.Final
# - com.google.firebase:firebase-admin:5.9.0 wanted version 4.1.17.Final
# - io.netty:netty-handler-proxy:4.1.22.Final wanted version 4.1.22.Final
    {"artifact": "io.netty:netty-transport:4.1.22.Final", "lang": "java", "sha1": "3bd455cd9e5e5fb2e08fd9cd0acfa54c079ca989", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_netty_netty_transport", "actual": "@io_netty_netty_transport//jar", "bind": "jar/io/netty/netty_transport"},
# duplicates in io.opencensus:opencensus-api promoted to 0.15.0
# - io.grpc:grpc-core:1.12.0 wanted version 0.11.0
# - io.opencensus:opencensus-contrib-grpc-metrics:0.15.0 wanted version 0.15.0
    {"artifact": "io.opencensus:opencensus-api:0.15.0", "lang": "java", "sha1": "9a098392b287d7924660837f4eba0ce252013683", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_opencensus_opencensus_api", "actual": "@io_opencensus_opencensus_api//jar", "bind": "jar/io/opencensus/opencensus_api"},
    {"artifact": "io.opencensus:opencensus-contrib-grpc-metrics:0.15.0", "lang": "java", "sha1": "092d8f796006619ce4e86d987f9d441c6155bef9", "repository": "https://repo.maven.apache.org/maven2/", "name": "io_opencensus_opencensus_contrib_grpc_metrics", "actual": "@io_opencensus_opencensus_contrib_grpc_metrics//jar", "bind": "jar/io/opencensus/opencensus_contrib_grpc_metrics"},
    {"artifact": "javax.annotation:javax.annotation-api:1.3.2", "lang": "java", "sha1": "934c04d3cfef185a8008e7bf34331b79730a9d43", "repository": "https://repo.maven.apache.org/maven2/", "name": "javax_annotation_javax_annotation_api", "actual": "@javax_annotation_javax_annotation_api//jar", "bind": "jar/javax/annotation/javax_annotation_api"},
    {"artifact": "javax.annotation:jsr250-api:1.0", "lang": "java", "sha1": "5025422767732a1ab45d93abfea846513d742dcf", "repository": "https://repo.maven.apache.org/maven2/", "name": "javax_annotation_jsr250_api", "actual": "@javax_annotation_jsr250_api//jar", "bind": "jar/javax/annotation/jsr250_api"},
    {"artifact": "javax.inject:javax.inject:1", "lang": "java", "sha1": "6975da39a7040257bd51d21a231b76c915872d38", "repository": "https://repo.maven.apache.org/maven2/", "name": "javax_inject_javax_inject", "actual": "@javax_inject_javax_inject//jar", "bind": "jar/javax/inject/javax_inject"},
    {"artifact": "joda-time:joda-time:2.9.2", "lang": "java", "sha1": "36d6e77a419cb455e6fd5909f6f96b168e21e9d0", "repository": "https://repo.maven.apache.org/maven2/", "name": "joda_time_joda_time", "actual": "@joda_time_joda_time//jar", "bind": "jar/joda_time/joda_time"},
    {"artifact": "junit:junit:4.12", "lang": "java", "sha1": "2973d150c0dc1fefe998f834810d68f278ea58ec", "repository": "https://repo.maven.apache.org/maven2/", "name": "junit_junit", "actual": "@junit_junit//jar", "bind": "jar/junit/junit"},
# duplicates in log4j:log4j promoted to 1.2.17
# - commons-logging:commons-logging:1.2 wanted version 1.2.15
# - commons-logging:commons-logging:1.2 wanted version 1.2.17
    {"artifact": "log4j:log4j:1.2.17", "lang": "java", "sha1": "5af35056b4d257e4b64b9e8069c0746e8b08629f", "repository": "https://repo.maven.apache.org/maven2/", "name": "log4j_log4j", "actual": "@log4j_log4j//jar", "bind": "jar/log4j/log4j"},
    {"artifact": "net.bytebuddy:byte-buddy-agent:1.7.9", "lang": "java", "sha1": "a6c65f9da7f467ee1f02ff2841ffd3155aee2fc9", "repository": "https://repo.maven.apache.org/maven2/", "name": "net_bytebuddy_byte_buddy_agent", "actual": "@net_bytebuddy_byte_buddy_agent//jar", "bind": "jar/net/bytebuddy/byte_buddy_agent"},
    {"artifact": "net.bytebuddy:byte-buddy:1.7.9", "lang": "java", "sha1": "51218a01a882c04d0aba8c028179cce488bbcb58", "repository": "https://repo.maven.apache.org/maven2/", "name": "net_bytebuddy_byte_buddy", "actual": "@net_bytebuddy_byte_buddy//jar", "bind": "jar/net/bytebuddy/byte_buddy"},
    {"artifact": "org.apache.commons:commons-csv:1.5", "lang": "java", "sha1": "e10f140af5b82167640f254fa9d88e35ad74329c", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_commons_commons_csv", "actual": "@org_apache_commons_commons_csv//jar", "bind": "jar/org/apache/commons/commons_csv"},
    {"artifact": "org.apache.commons:commons-lang3:3.7", "lang": "java", "sha1": "557edd918fd41f9260963583ebf5a61a43a6b423", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_commons_commons_lang3", "actual": "@org_apache_commons_commons_lang3//jar", "bind": "jar/org/apache/commons/commons_lang3"},
    {"artifact": "org.apache.httpcomponents:fluent-hc:4.5.5", "lang": "java", "sha1": "1af996e860dc8e246f4aca944f76f0513d41acc0", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_httpcomponents_fluent_hc", "actual": "@org_apache_httpcomponents_fluent_hc//jar", "bind": "jar/org/apache/httpcomponents/fluent_hc"},
# duplicates in org.apache.httpcomponents:httpclient promoted to 4.5.5
# - com.google.http-client:google-http-client:1.23.0 wanted version 4.0.1
# - org.apache.httpcomponents:fluent-hc:4.5.5 wanted version 4.5.5
# - org.eclipse.jgit:org.eclipse.jgit:4.10.0.201712302008-r wanted version 4.5.2
    {"artifact": "org.apache.httpcomponents:httpclient:4.5.5", "lang": "java", "sha1": "1603dfd56ebcd583ccdf337b6c3984ac55d89e58", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_httpcomponents_httpclient", "actual": "@org_apache_httpcomponents_httpclient//jar", "bind": "jar/org/apache/httpcomponents/httpclient"},
    {"artifact": "org.apache.httpcomponents:httpcore:4.4.9", "lang": "java", "sha1": "a86ce739e5a7175b4b234c290a00a5fdb80957a0", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_httpcomponents_httpcore", "actual": "@org_apache_httpcomponents_httpcore//jar", "bind": "jar/org/apache/httpcomponents/httpcore"},
    {"artifact": "org.apache.pdfbox:fontbox:2.0.8", "lang": "java", "sha1": "52f852fcfc7481d45efdffd224eb78b85981b17b", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_pdfbox_fontbox", "actual": "@org_apache_pdfbox_fontbox//jar", "bind": "jar/org/apache/pdfbox/fontbox"},
    {"artifact": "org.apache.pdfbox:pdfbox:2.0.8", "lang": "java", "sha1": "17bdf273d66f3afe41eedb9d3ab6a7b819c44a0c", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_apache_pdfbox_pdfbox", "actual": "@org_apache_pdfbox_pdfbox//jar", "bind": "jar/org/apache/pdfbox/pdfbox"},
# duplicates in org.checkerframework:checker-compat-qual promoted to 2.3.0
# - com.google.dagger:dagger-producers:2.15 wanted version 2.3.0
# - com.google.guava:guava:24.0-android wanted version 2.0.0
    {"artifact": "org.checkerframework:checker-compat-qual:2.3.0", "lang": "java", "sha1": "69cb4fea55a9d89b8827d107f17c985cc1a76052", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_checkerframework_checker_compat_qual", "actual": "@org_checkerframework_checker_compat_qual//jar", "bind": "jar/org/checkerframework/checker_compat_qual"},
    {"artifact": "org.codehaus.jackson:jackson-core-asl:1.9.11", "lang": "java", "sha1": "e32303ef8bd18a5c9272780d49b81c95e05ddf43", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_codehaus_jackson_jackson_core_asl", "actual": "@org_codehaus_jackson_jackson_core_asl//jar", "bind": "jar/org/codehaus/jackson/jackson_core_asl"},
    {"artifact": "org.codehaus.mojo:animal-sniffer-annotations:1.14", "lang": "java", "sha1": "775b7e22fb10026eed3f86e8dc556dfafe35f2d5", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_codehaus_mojo_animal_sniffer_annotations", "actual": "@org_codehaus_mojo_animal_sniffer_annotations//jar", "bind": "jar/org/codehaus/mojo/animal_sniffer_annotations"},
    {"artifact": "org.eclipse.jgit:org.eclipse.jgit:4.10.0.201712302008-r", "lang": "java", "sha1": "61bda423283956d35117a0c1f993e752d12f3132", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_eclipse_jgit_org_eclipse_jgit", "actual": "@org_eclipse_jgit_org_eclipse_jgit//jar", "bind": "jar/org/eclipse/jgit/org_eclipse_jgit"},
    {"artifact": "org.hamcrest:hamcrest-core:1.3", "lang": "java", "sha1": "42a25dc3219429f0e5d060061f71acb49bf010a0", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_hamcrest_hamcrest_core", "actual": "@org_hamcrest_hamcrest_core//jar", "bind": "jar/org/hamcrest/hamcrest_core"},
    {"artifact": "org.javassist:javassist:3.22.0-GA", "lang": "java", "sha1": "3e83394258ae2089be7219b971ec21a8288528ad", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_javassist_javassist", "actual": "@org_javassist_javassist//jar", "bind": "jar/org/javassist/javassist"},
    {"artifact": "org.json:json:20180130", "lang": "java", "sha1": "26ba2ec0e791a32ea5dfbedfcebf36447ee5b12c", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_json_json", "actual": "@org_json_json//jar", "bind": "jar/org/json/json"},
    {"artifact": "org.jsoup:jsoup:1.11.2", "lang": "java", "sha1": "e3eeb8a0b4ce1db246059a41e353cd7413dad226", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_jsoup_jsoup", "actual": "@org_jsoup_jsoup//jar", "bind": "jar/org/jsoup/jsoup"},
    {"artifact": "org.mockito:mockito-core:2.15.0", "lang": "java", "sha1": "b84bfbbc29cd22c9529409627af6ea2897f4fa85", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_mockito_mockito_core", "actual": "@org_mockito_mockito_core//jar", "bind": "jar/org/mockito/mockito_core"},
    {"artifact": "org.objenesis:objenesis:2.6", "lang": "java", "sha1": "639033469776fd37c08358c6b92a4761feb2af4b", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_objenesis_objenesis", "actual": "@org_objenesis_objenesis//jar", "bind": "jar/org/objenesis/objenesis"},
# duplicates in org.slf4j:slf4j-api promoted to 1.7.25
# - com.google.firebase:firebase-admin:5.9.0 wanted version 1.7.25
# - org.eclipse.jgit:org.eclipse.jgit:4.10.0.201712302008-r wanted version 1.7.2
# - org.slf4j:slf4j-simple:1.7.25 wanted version 1.7.25
    {"artifact": "org.slf4j:slf4j-api:1.7.25", "lang": "java", "sha1": "da76ca59f6a57ee3102f8f9bd9cee742973efa8a", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_slf4j_slf4j_api", "actual": "@org_slf4j_slf4j_api//jar", "bind": "jar/org/slf4j/slf4j_api"},
    {"artifact": "org.slf4j:slf4j-simple:1.7.25", "lang": "java", "sha1": "8dacf9514f0c707cbbcdd6fd699e8940d42fb54e", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_slf4j_slf4j_simple", "actual": "@org_slf4j_slf4j_simple//jar", "bind": "jar/org/slf4j/slf4j_simple"},
    {"artifact": "org.threeten:threetenbp:1.3.3", "lang": "java", "sha1": "3ea31c96676ff12ab56be0b1af6fff61d1a4f1f2", "repository": "https://repo.maven.apache.org/maven2/", "name": "org_threeten_threetenbp", "actual": "@org_threeten_threetenbp//jar", "bind": "jar/org/threeten/threetenbp"},
    ]

def maven_dependencies(callback = declare_maven):
    for hash in list_dependencies():
        callback(hash)
