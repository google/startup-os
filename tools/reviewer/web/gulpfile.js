const path = require('path');
const fs = require('fs-extra');  
const gulp = require('gulp');
const clean = require('gulp-clean');
const { exec } = require('child_process');

const protoPath = './src/app/shared/shell/proto';
const codeReviewProtoRelativePath = 'tools/reviewer/service/code_review.proto';
// from 'startup-os/tools/reviewer/web' to 'startup-os'
const startuposPath = path.resolve('../../../');

gulp.task('default', ['protoc']);

gulp.task('protoc', () => {
  // Create a proto folder
  if (!fs.existsSync(protoPath)) {
    fs.mkdirSync(protoPath);
  }

  // Copy all packages to the proto folder
  const codeReviewProtoPath = path.join(
    startuposPath,
    codeReviewProtoRelativePath
  );
  var protoImportList = [];
  const codeReviewFilename = path.parse(codeReviewProtoRelativePath).base;
  protoImportList.push(codeReviewFilename);
  const newCodeReviewPath = path.join(protoPath, codeReviewFilename);
  fs.copySync(codeReviewProtoPath, newCodeReviewPath);
  const codeReviewContent = fs.readFileSync(newCodeReviewPath, 'utf8');
  const codeReviewLines = codeReviewContent.split('\n');
  for (let line of codeReviewLines) {
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
      const newPackagePath = path.join(protoPath, importPath);
      fs.copySync(packagePath, newPackagePath);
      protoImportList.push(importPath);
    }
  }

  // Create proto functions from the packages in the proto folder
  const protoImport = protoImportList.join(' ');
  exec(
    'protoc ' +
      `--proto_path=${protoPath} ` +
      `--js_out=import_style=commonjs,binary:${protoPath} ` +
      '--plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts ' +
      `--ts_out=${protoPath} ` +
      protoImport,
    protocOnLoad
  );

  // When protoc finished generating proto functions
  function protocOnLoad(error) {
    if (error) {
      throw error;
    }

    // Remove all *.proto files from the proto folder
    gulp.src(path.join(protoPath, '**/*.proto'))
      .pipe(clean())
      .on('data', () => {})
      .on('end', removingOnLoad);
  }

  // When all *.proto files are removed
  function removingOnLoad() {
    console.log('Proto functions is successfully created.');
  }
});
