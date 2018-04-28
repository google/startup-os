package examples.dagger.example2;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = { Class2Module.class })
public interface SomeComponent {
  Class3 getClass3Object();
}