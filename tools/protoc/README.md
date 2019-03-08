# How to create proto functions

## Installation 
Run `npm install` to install node modules

## Setting
A project must contain `proto.json` file with required paths.  
Example:
```
{ 
  // Path to folder, where proto functions will be located.
  // The path is relative to the project
  "exportPath": "src/app/core/proto",
  // List with paths to each .proto file, which are used in the project.
  // The paths are relative to the workspace
  "protoList": [
    "hasadna/projects/angular-proto-firestore/proto/data.proto",
    "startup-os/tools/reviewer/reviewer.proto"
  ]
}
```

## Running
Run `node protoc.js <relative path to your project>` to generate proto functions.  
E.g. `node protoc.js hasadna/projects/angular-proto-firestore`  

No need to set path to your project, if you run the script from project root.  
`node /path/to/script/protoc.js`  

## Workspace apps runner
You can run protoc with `workspacerun`:
```
workspacerun protoc
```

## Supported platforms
Supported on Linux and Mac only.
