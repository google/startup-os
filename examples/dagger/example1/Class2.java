package examples.dagger.example1;

import javax.inject.Inject;

class Class2 {
  private final Class1Interface class1Object;

  @Inject
  Class2(Class1Interface class1Object) {
    this.class1Object = class1Object;
  }

  public void run() {
    class1Object.setTrue();
    class1Object.setFalse();
  }
}