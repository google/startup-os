# Renaming proto fields

Changing a field name will not affect protobuf encoding or 
compatibility between applications that use proto definitions 
which differ only by field names.
The binary protobuf encoding is based on tag numbers, so that 
is what you need to preserve.
 
## Contents of this tutorial  
- <walkthrough-editor-open-file 
  	    filePath="startup-os/tutorials/proto_rename/person.proto" 
  	    text="person.proto">
      </walkthrough-editor-open-file> 
It's a proto definition. 
We will rename some fields in next steps and see what will happen.

- <walkthrough-editor-open-file 
  	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
  	    text="PersonTool.java">
      </walkthrough-editor-open-file> 
Contains main method and methods to write and read a proto file in binary format.


## Preliminary setup
As _most_ of our code, as well as `bazel` itself is written in Java, `bazel` needs
to know where to find JDK. Unfortunately, it seems that first-in-mind Google Cloud Build is configured to
*run* Java programs, not to _build_ them - this is why `JAVA_HOME` is pointing to **JRE**
which confuses `bazel`. To fix it, before running rest of the tutorial, run either of:
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
```
to use system JDK or
```bash
unset JAVA_HOME
```
to use JDK embedded in bazel

## First launch
- Run the command to build targets:
```bash
bazel build //tutorials/proto_rename:all
```
- Look at `writePerson()` method of <walkthrough-editor-open-file 
                                    	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
                                    	    text="PersonTool.java">
                                        </walkthrough-editor-open-file> class. 
This method is creating a new Person which will be saved to `person.protobin` file.
- Run the command to create `person.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- write
```
- Run the command to read `person.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```

## Renaming a field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/person.proto" 
       	    text="person.proto">
           </walkthrough-editor-open-file> file.
- Change `string name = 1` field to `string full_name = 1`
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and and change 
`setName("John")` setter to `setFullName("John")` in `writePerson()` method.
- Run the command to read `person.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.protobin` and `string name` field 
was changed to `string full_name`.

## Renaming an enum value
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/person.proto" 
       	    text="person.proto">
           </walkthrough-editor-open-file> file
- Change `OLIVES_AND_PINEAPPLE = 1` field to `SEAFOOD = 1`
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and change 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPPLE)` 
setter to 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.SEAFOOD)` 
in `writePerson()` method.
- Run the command to read `person.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.protobin` file 
and `OLIVES_AND_PINEAPPLE` field was changed to `SEAFOOD`.
 
## Renaming a message
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/person.proto" 
       	    text="person.proto">
           </walkthrough-editor-open-file> file.

- Change `message Person` to `message Employee`.
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and change:
1) `import com.google.startupos.tutorials.protorename.Protos.Person` 
to `import com.google.startupos.tutorials.protorename.Protos.Employee`.
2) All mentions of `Person` to `Employee` in `writePerson()` 
and `readPerson()` methods.
- Run the command to read `person.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.protobin` file.