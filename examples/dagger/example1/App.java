package examples.dagger.example1;

import dagger.Component;
import javax.inject.Singleton;

public class App {
  public static void main(String[] args) {
    SomeComponent someComponent = DaggerSomeComponent.builder().build();
    someComponent.getClass2Object().run();
  }
}