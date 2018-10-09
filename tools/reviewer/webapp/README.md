# Reviewer webapp
The Reviewer front-end is an Angular webapp.

To set up your development environment, follow these steps:

## Installation
Install these:
* [node](https://nodejs.org/) LTS version
* [npm](https://www.npmjs.com/)  
* [google-protobuf](https://github.com/protocolbuffers/protobuf/releases), version 3.5 or above.

Optional:
* [firebase](https://firebase.google.com/docs/hosting/quickstart), to be able to deploy.  
* [angular](https://angular.io/), to use `ng` features.  

Run `npm install` to install npm modules  

Run `npm run protoc` to generate proto functions.  

## Running the server locally
Run `npm start` to start a dev server. Navigate to `http://localhost:4200/`  
Run `npm run build` to make a build.  
Run `npm run deploy` to publish the app on the server.  
Run `npm run functions` to update cloud functions on the server.  

How to use firebase hosting:  
https://firebase.google.com/docs/hosting/quickstart  

How to use firebase functions:  
https://firebase.google.com/docs/functions/get-started

## Updating protos
After every proto update, run `npm run protoc`.

## Supported platforms
Development is supported on Linux and Mac only (`npm run protoc` doesn't support Windows).
