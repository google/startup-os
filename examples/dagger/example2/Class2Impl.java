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

package com.google.startupos.examples.dagger.example2;

import javax.inject.Inject;

class Class2Impl implements Class2Interface {
  private final Class1Interface class1Object;

  @Inject
  Class2Impl(Class1Interface class1Object) {
    this.class1Object = class1Object;
  }

  @Override
  public void printClass1ObjectStatus() {
    System.out.println(
        "Class2Impl.printClass1ObjectStatus(): Class1Object's booleanValue is "
            + class1Object.getBooleanValue());
  }
}

