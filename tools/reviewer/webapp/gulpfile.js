const path = require('path');
const fs = require('fs-extra');  
const gulp = require('gulp');
const clean = require('gulp-clean');
const { exec } = require('child_process');

const protoPath = './src/app/core/proto/functions';
const codeReviewProtoRelativePath = 'tools/reviewer/local_server/service/code_review.proto';
// from 'startup-os/tools/reviewer/webapp' to 'startup-os'
const startuposPath = path.resolve('../../../');

gulp.task('default', ['protoc']);

/*
  Why we need gulp to generate proto functions?
  1. We have to know all paths to *.proto files, before running protoc.
    To not set manually all the paths and to not be dependent on them,
    we get children paths from code_review.proto.
  2. We can't use a parent path with protoc. Such as '../../../'.
    So to keep all manipulation with web app inside the web app project, we
    use gulp.
  3. Proto definitions have imports with exact paths.
    e.g. 'common/repo/repo.proto'
    If we don't want to edit proto files, then we have to keep that structure.
    It's much easier to do it with gulp.
*/
gulp.task('protoc', () => {
  // Create a proto folder
  if (!fs.existsSync(protoPath)) {
    fs.mkdirSync(protoPath);
  }

  var protoImportList = [];
  // Copy all packages to the proto folder
  function copyPackage(relativePath) {
    const absolutePath = path.join(
      startuposPath,
      relativePath
    );
    const filename = path.parse(absolutePath).base;
    protoImportList.push(filename);
    const newPackagePath = path.join(protoPath, filename);
    fs.copySync(absolutePath, newPackagePath);

    return newPackagePath;
  }
  const newCodeReviewPath = copyPackage(codeReviewProtoRelativePath);

  // Copy children of code_review.proto to the proto folder
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
    console.log('Proto functions are successfully created.');
  }
});
