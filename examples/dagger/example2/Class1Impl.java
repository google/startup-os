package examples.dagger.example2;

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