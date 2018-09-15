## You need installed
Required to use Reviewer locally:
[node](https://nodejs.org/) LTS version
[npm](https://www.npmjs.com/)  
[protoc](https://github.com/protocolbuffers/protobuf/releases)  
Optional:  
[firebase](https://firebase.google.com/docs/hosting/quickstart) to be able to deploy.  
[angular](https://angular.io/) to use `ng` features.  

## For the first time
Run `npm install` to install npm modules  

Run `npm run protoc` to generate proto functions.  
You need google-protobuf 3.5 or above installed on your machine.  
You need to run `protoc` after every proto change.
Supported on Linux and Mac only.  

## Development
Run `npm start` to start a dev server. Navigate to `http://localhost:4200/`  
Run `npm run build` to make a build.  
Run `firebase deploy` to publish the app on the server.  
How to use firebase:  
https://firebase.google.com/docs/hosting/quickstart  
