/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.startupos.examples.errorprone;

// import com.google.startupos.proto_vs_json.Protos;

public class Hello {
  public static void main(String[] args) {

    // <editor-fold desc="Uncomment to test StringFmtInPrintMethodsCheck">
    // System.err.print(String.format("Hello: %s\n", "World"));
    // </editor-fold>

    // <editor-fold desc="Uncomment to test ProtobufCheck">
    // Protos.Person.newBuilder().build();
    // </editor-fold>
  }
}

