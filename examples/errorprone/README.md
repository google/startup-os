# Error-prone
[error-prone](http://errorprone.info/) is a library intended to
prevent commonly-made Java mistakes. It also makes it easy to 
integrate custom-built checks.

# StringFmtInPrintMethodsCheck
Checks that `System.<>.printf(<>)` is called instead of
`System.<>.print(String.format(<>))`. Adapted from 
[MyCustomCheck](https://github.com/google/error-prone/blob/master/examples/plugin/bazel/java/com/google/errorprone/sample/MyCustomCheck.java)
example in `error-prone`

# ProtobufCheck
Checks that `.getDefaultInstance()` is called instead of 
`.newBuilder().build()` on 
[protobuf](https://github.com/google/protobuf/tree/master/java) 
messages