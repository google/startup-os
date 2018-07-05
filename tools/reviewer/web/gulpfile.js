const path = require('path');
const fs = require('fs');
const gulp = require('gulp');
const { exec } = require('child_process');

const pathToProto = './src/assets';
const shellPath = './src/app/shared/shell/proto';
const protoName = 'code-review';
const protoPath = path.join(pathToProto, protoName + '.proto');

gulp.task('default', ['protoc']);

gulp.task('protoc', () => {
  // Convert proto definition to proto functions
  switch (process.platform) {
    case 'win32': {
      // Windows
      exec(
        'protoc ' +
          '--js_out=import_style=commonjs,binary:./ ' +
          '--plugin=protoc-gen-ts=%CD%/node_modules/.bin/protoc-gen-ts.cmd ' +
          '--ts_out=./ ' +
          protoPath,
        protocOnLoad
      );
      break;
    }
    default: {
      // Linux and Mac
      exec(
        'protoc ' +
          '--js_out=import_style=commonjs,binary:./ ' +
          '--plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts ' +
          '--ts_out=./ ' +
          protoPath,
        protocOnLoad
      );
    }
  }

  // Move proto functions to angular files
  function protocOnLoad(error) {
    if (error) {
      throw error;
    }

    // Proto functions:
    const jsFunctions = protoName + '_pb.js';
    const tsFunctions = protoName + '_pb.d.ts';

    // The folder is ignored by git
    // We need to create it, if it doesn't exist
    if (!fs.existsSync(shellPath)) {
      fs.mkdirSync(shellPath);
    }

    // Move js functions to shell
    const jsPath = path.join(pathToProto, jsFunctions);
    if (fs.existsSync(jsPath)) {
      fs.renameSync(jsPath, path.join(shellPath, jsFunctions));
    }

    // Move ts functions to shell
    const tsPath = path.join(pathToProto, tsFunctions);
    if (fs.existsSync(tsPath)) {
      fs.renameSync(tsPath, path.join(shellPath, tsFunctions));
    }
  }
});
