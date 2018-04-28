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

package examples.dagger.example2;

import dagger.Lazy;
import javax.inject.Inject;

class Class3 {
  private final Lazy<Class1Interface> lazyClass1Object; // Create a possibly costly Class1 only when we use it.
  private final Class2Impl class2Object;

  @Inject
  Class3(Lazy<Class1Interface> lazyClass1Object, Class2Impl class2Object) {
    this.lazyClass1Object = lazyClass1Object;
    this.class2Object = class2Object;
  }

  public void run() {
    lazyClass1Object.get().setTrue();
    System.out.println("Calling class2Object.printClass1ObjectStatus():");
    class2Object.printClass1ObjectStatus();
    System.out.println();
    lazyClass1Object.get().setFalse();
    System.out.println("Calling class2Object.printClass1ObjectStatus():");
    class2Object.printClass1ObjectStatus();
  }
}
