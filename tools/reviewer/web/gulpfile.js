const path = require('path');
const fs = require('fs-extra');  
const gulp = require('gulp');
const clean = require('gulp-clean');
const mv = require('mv');
const { exec } = require('child_process');

const tempPath = './temp';
const shellPath = './src/app/shared/shell/proto';
const codeReviewProtoPathRelative = 'tools/reviewer/service/code_review.proto';
// from 'startup-os/tools/reviewer/web' to 'startup-os'
const startuposPath = path.resolve('../../../');

gulp.task('default', ['protoc']);

gulp.task('protoc', () => {
  // Create temporary folder
  if (!fs.existsSync(tempPath)) {
    fs.mkdirSync(tempPath);
  }

  // Copy all packages to the temporary folder
  const codeReviewProtoPath = path.join(
    startuposPath,
    codeReviewProtoPathRelative
  );
  var protoImportList = [];
  const codeReviewFilename = path.parse(codeReviewProtoPathRelative).base;
  protoImportList.push(codeReviewFilename);
  const newCodeReviewPath = path.join(tempPath, codeReviewFilename);
  fs.copySync(codeReviewProtoPath, newCodeReviewPath);
  const codeReviewContent = fs.readFileSync(newCodeReviewPath, 'utf8');
  const codeReviewLines = codeReviewContent.split('\n');
  for(let line of codeReviewLines) {
    // Get children paths from imports in code_review.proto
    if (line.startsWith('import')) {
      // Everything between quotes
      const regexExpression = /"(.*)"/;
      
      const importPath = line.match(regexExpression)[1];
      
      if (importPath.startsWith('google')) {
        // protoc is able to work with google imports by itself.
        // Unfortunately on unix system only..
        continue;
      }

      const packagePath = path.join(startuposPath, importPath);
      if (!fs.existsSync(packagePath)) {
        throw new Error('Package not found: ' + importPath);
      }
      const newPackagePath = path.join(tempPath, importPath);
      fs.copySync(packagePath, newPackagePath);
      protoImportList.push(importPath);
    }
  }

  // Create proto functions from the packages in the temporary folder
  const protoImport = protoImportList.join(' ');
  exec(
    'protoc ' +
      `--proto_path=${tempPath} ` +
      `--js_out=import_style=commonjs,binary:${tempPath} ` +
      '--plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts ' +
      `--ts_out=${tempPath} ` +
      protoImport,
    protocOnLoad
  );

  // When protoc finished generating proto functions
  function protocOnLoad(error) {
    if (error) {
      throw error;
    }

    // Remove all *.proto files
    gulp.src(path.join(tempPath, '**/*.proto'))
      .pipe(clean())
      .on('data', () => {})
      .on('end', removingOnLoad);
  }

  // When all *.proto files are removed
  function removingOnLoad() {
    // Move generated proto functions inside angular
    mv(tempPath, shellPath, movingOnLoad)
  }

  // When all proto function are moved inside angular
  function movingOnLoad(error) {
    if (error) {
      throw error;
    }
    console.log('Proto functions is successfully created.');
  }
});
