package examples.dagger.example1;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = { Class1Module.class })
public interface SomeComponent {
  Class2 getClass2Object();
}