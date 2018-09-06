package tutorials.proto_rename;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tutorials.proto_rename.Protos.Person;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

public class PersonWriter {
  private FileUtils fileUtils;

  @Inject
  PersonWriter() {
    fileUtils = DaggerPersonWriter_PersonWriterComponent.create().getFileUtils();
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  interface PersonWriterComponent {
    FileUtils getFileUtils();
  }

  void writePerson(Person person, String path) {
    fileUtils.writeProtoBinaryUnchecked(person, path);
  }

  public static void main(String[] args) {
    PersonWriter writer = new PersonWriter();
    // create your person here
    Person person =
        Person.newBuilder()
            .setId(2)
            .setName("John")
            .addPhone(
                Person.PhoneNumber.newBuilder()
                    .setNumber(7654321)
                    .setType(Person.PhoneType.MOBILE)
                    .build())
            .build();
    writer.writePerson(person, "tutorials/proto_rename/person.pb");
  }
}

