package tutorials.proto_rename;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tutorials.proto_rename.Protos.Person;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

public class PersonReader {
  private FileUtils fileUtils;

  @Inject
  PersonReader() {
    fileUtils = DaggerPersonReader_PersonReaderComponent.create().getFileUtils();
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  interface PersonReaderComponent {
    FileUtils getFileUtils();
  }

  Person readPerson(String path, Person.Builder builder) {
    return (Person) fileUtils.readProtoBinaryUnchecked(path, builder);
  }

  public static void main(String[] args) {
    PersonReader reader = new PersonReader();
    Person person = reader.readPerson("tutorials/proto_rename/person.pb", Person.newBuilder());
    System.out.println(person);
  }
}

