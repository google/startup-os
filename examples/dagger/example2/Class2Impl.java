package examples.dagger.example2;

import javax.inject.Inject;

class Class2Impl implements Class2Interface {
  private final Class1Interface class1Object;

  @Inject
  Class2Impl(Class1Interface class1Object) {
    this.class1Object = class1Object;
  }

  @Override
  public void printClass1ObjectStatus() {
    System.out.println("Class2Impl.printClass1ObjectStatus(): Class1Object's booleanValue is " + class1Object.getBooleanValue());
  }
}