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