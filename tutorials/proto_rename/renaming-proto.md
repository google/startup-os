# Renaming proto fields

Changing a field name will not affect protobuf encoding or 
compatibility between applications that use proto definitions 
which differ only by field names.
The binary protobuf encoding is based on tag numbers, so that 
is what you need to preserve.
 
## Contents of this tutorial  
- <walkthrough-editor-open-file 
  	    filePath="startup-os/tutorials/proto_rename/persons.proto" 
  	    text="persons.proto">
      </walkthrough-editor-open-file> 
It's a proto definition. 
We will rename some fields in next steps and see what will happen.

- <walkthrough-editor-open-file 
  	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
  	    text="PersonTool.java">
      </walkthrough-editor-open-file> 
Contains main method and methods to write and read a proto file in binary format.

## First launch
- Run the command to build targets:
```bash
bazel build //tutorials/proto_rename:all
```
- Look at `writePersons()` method of <walkthrough-editor-open-file 
                                    	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
                                    	    text="PersonTool.java">
                                        </walkthrough-editor-open-file> class. 
This method is creating a new Persons which will be saved to `persons.protobin` file.
- Run the command to create `persons.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- write
```
- Run the command to read `persons.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```

## Renaming scalar field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/persons.proto" 
       	    text="persons.proto">
           </walkthrough-editor-open-file> file.
- Change `string name = 1` field to `string full_name = 1`
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and and change 
`setName("John")` setter to `setFullName("John")` in `writePersons()` method.
- Run the command to read `persons.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `persons.protobin` and `string name` field 
was changed to `string full_name`.

## Renaming enum field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/persons.proto" 
       	    text="persons.proto">
           </walkthrough-editor-open-file> file
- Change `OLIVES_AND_PINEAPPLE = 1` field to `SEAFOOD = 1`
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and change 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPPLE)` 
setter to 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.SEAFOOD)` 
in `writePersons()` method.
- Run the command to read `persons.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `persons.protobin` file 
and `OLIVES_AND_PINEAPPLE` field was changed to `SEAFOOD`.
 
## Renaming message field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/persons.proto" 
       	    text="persons.proto">
           </walkthrough-editor-open-file> file.

- Change `repeated Person persons = 1` field to `repeated Person people = 1`.
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and change 
`addPersons(...)` setter to `addPeople(...)` in `writePersons()` method.
- Run the command to read `persons.protobin` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `persons.protobin` file and 
`persons` field was changed to `people`.