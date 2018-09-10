# Renaming proto fields

Changing a field name will not affect protobuf encoding or 
compatibility between applications that use proto definitions 
which differ only by field names.
The binary protobuf encoding is based on tag numbers, so that 
is what you need to preserve.
 
## Contents of this tutorial  
- **person.proto** 
It's a proto definition. 
We will rename some fields in next steps and see what will happen.

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/person.proto" 
	    text="person.proto">
    </walkthrough-editor-open-file>

- **PersonWriter.java** 
Writes a Person message to `person.pb` file.

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonWriter.java" 
	    text="PersonWriter.java">
    </walkthrough-editor-open-file>

- **PersonReader.java** 
Reads a Person message from `person.pb` file, which creates `PersonWriter`.

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonReader.java" 
	    text="PersonReader.java">
    </walkthrough-editor-open-file>

- **PersonTool.java** 
Contains the `main` method and manages `PersonReader` and `PersonWriter`.

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
	    text="PersonTool.java">
    </walkthrough-editor-open-file>

## First launch
- Run the command to build targets:
```bash
bazel build //tutorials/proto_rename:all
```
- Look at `writePerson()` method of `PersonTool.java` class. 
This method is creating a new Person which will be saved to `person.pb` file.

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
	    text="PersonTool.java">
    </walkthrough-editor-open-file>
 
- Run the command to creates `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- write
```
- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```

## Renaming scalar field
- Open `person.proto` file:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/person.proto" 
	    text="person.proto">
    </walkthrough-editor-open-file>

- Change `string name = 1` field to `string full_name = 1`
- Save changes.
- Open `PersonTool.java` and comment the body of `writePerson()` method:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
	    text="PersonTool.java"
    </walkthrough-editor-open-file>

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
Remember, when you rename proto field you need also rename setter 
for this field in the code.

## Renaming enum field
- Open `person.proto` file:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/person.proto" 
	    text="person.proto">
    </walkthrough-editor-open-file>

- Change `OLIVES_AND_PINEAPPLE = 1` field to `SEAFOOD = 1`
- Open `PersonTool.java` and change 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPPLE)` 
setter to 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.SEAFOOD)` 
in `writePerson()` method:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
	    text="PersonTool.java"
    </walkthrough-editor-open-file>

- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.pb` file 
and `OLIVES_AND_PINEAPPLE` field was changed to `SEAFOOD`.
 
## Renaming message field
- Open `person.proto` file:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/person.proto" 
	    text="person.proto">
    </walkthrough-editor-open-file>

- Change `FavoritePizzaTopping favorite_pizza_topping = 3` field 
to `FavoritePizzaTopping favorite_pizza = 3`.
- Open `PersonTool.java` and change 
`setFavoritePizzaTopping(Person.FavoritePizzaTopping.SEAFOOD)` setter 
to `setFavoritePizza(Person.FavoritePizzaTopping.SEAFOOD)` 
in `writePerson()` method:

    <walkthrough-editor-open-file 
	    filePath="startup-os/tutorials/proto_rename/PersonTool.java" 
	    text="PersonTool.java"
    </walkthrough-editor-open-file>

- Run the command to reads `person.pb` file:
```bash
bazel run //tutorials/proto_rename:person_tool -- read
```
You can see that we still read `person.pb` file and 
`favorite_pizza_topping` field was changed to `favorite_pizza`.