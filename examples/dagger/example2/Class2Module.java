package examples.dagger.example2;

import dagger.Module;
import dagger.Provides;

@Module(includes = Class1Module.class)
class Class2Module {
  @Provides Class2Interface provideClass2Impl(Class2Impl class2Object) {
    return class2Object;
  }
}