# workspacerun
Workspace apps runner

## How to add workspacerun to your project
Run from your project:
```
npm i workspacerun --save-dev
```

## How to publish new version to npm
If you're admin of the package:
```
npm publish
```

If you're not admin:
* Ask one of the npm package admins to publish
or
* Ask one of the npm package admins to add you as package admin
[Managing npm team access](https://docs.npmjs.com/managing-team-access-to-org-packages)

## workspacerun apps
json file with apps map is located here:
```
startup-os/tools/workspacerun/apps.json
```
Example of structure of map point:
```
{
  // Name of an app
  "name": "protoc",
  // Relative path to the app
  "path": "startup-os/tools/protoc/protoc.js"
}
```

## Running
Run from your project: `workspacerun <name of an app>`  
Example: `workspacerun protoc`
