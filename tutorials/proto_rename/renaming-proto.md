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
Contains main method and methods to write and read protobinary file.

## First launch
- Run the command to build targets:
```bash
bazel build //tutorials/proto_rename:all
```
- Look at `writePerson()` method of <walkthrough-editor-open-file 
                                    	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
                                    	    text="PersonTool.java">
                                        </walkthrough-editor-open-file> class. 
This method is creating a new Person which will be saved to `person.pb` file.
- Run the command to creates `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- write
```
- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```

## Renaming scalar field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/person.proto" 
       	    text="person.proto">
           </walkthrough-editor-open-file> file.
- Change `string name = 1` field to `string full_name = 1`
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and comment the body of `writePerson()` method like this:
```
  private static void writePerson() {
//    Person person =
//        Person.newBuilder()
//            .setName("John")
//            .setId(1)
//            .setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPLE)
//            .build();
//
//    fileUtils.writeProtoBinaryUnchecked(person, personProtobinaryPath);
  }
```

- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.pb` and `string name` field 
was changed to `string full_name`.
- Uncomment the body of `writePerson()` method. 
- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
- It doesn't compile. When we renaming proto field we also need 
to change the setter for this field. 
Just change `setName("John")` to `setFullName("John")`. 
Run it one more time:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
**Remember, when you rename proto field you need also rename setter 
for this field in the code.**

## Renaming enum field
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
- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.pb` file 
and `OLIVES_AND_PINEAPPLE` field was changed to `SEAFOOD`.
 
## Renaming message field
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/person.proto" 
       	    text="person.proto">
           </walkthrough-editor-open-file> file.

- Change `FavoritePizzaTopping favorite_pizza_topping = 3` field 
to `FavoritePizzaTopping favorite_pizza = 3`.
- Open <walkthrough-editor-open-file 
       	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
       	    text="PersonTool.java">
           </walkthrough-editor-open-file> and change 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.SEAFOOD)` setter 
to `setFavoritePizza(Person.FavoritePizzaTopping.SEAFOOD)` 
in `writePerson()` method.
- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.pb` file and 
`favorite_pizza_topping` field was changed to `favorite_pizza`.