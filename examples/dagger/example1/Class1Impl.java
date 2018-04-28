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

package examples.dagger.example1;

class Class1Impl implements Class1Interface {
  boolean booleanValue;

  @Override public void setTrue() {
    this.booleanValue = true;
    System.out.println("Class1Impl.setTrue()");
  }

  @Override public void setFalse() {
    this.booleanValue = false;
    System.out.println("Class1Impl.setFalse()");
  }

  @Override public boolean getBooleanValue() {
    return booleanValue;
  }
}
