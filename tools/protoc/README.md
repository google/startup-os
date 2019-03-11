# How to create proto functions

## Installation 
Run from this folder: `npm install` to install node modules of protoc.  

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
Run from your project: `workspacerun protoc` to generate proto functions. [More info](https://github.com/google/startup-os/blob/master/tools/workspacerun/README.md)  

Alternative way, if you need to generate proto functions from outside of project root.  
Example: `node /path/to/script/protoc.js hasadna/projects/angular-proto-firestore`

## Supported platforms
Supported on Linux and Mac only.
