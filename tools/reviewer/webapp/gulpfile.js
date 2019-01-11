const path = require('path');
const fs = require('fs-extra');  
const gulp = require('gulp');
const clean = require('gulp-clean');
const { exec } = require('child_process');

const protoPath = './src/app/core/proto';
const functionsPath = path.join(protoPath, 'functions');
// List with proto files, which contain packages inside
protoList = [
  'tools/reviewer/local_server/service/code_review.proto',
  'tools/reviewer/reviewer_registry.proto',
];
// from 'startup-os/tools/reviewer/webapp' to 'startup-os'
const startuposPath = path.resolve('../../../');

gulp.task('default', ['protoc']);

/*
  Why we need gulp to generate proto functions?
  1. It's convenient. Less manual steps.
  2. We have to know all paths to *.proto files, before running protoc.
    To not set manually all the paths and to not be dependent on them,
    we get children paths from code_review.proto.
  3. We can't use a parent path with protoc. Such as '../../../'.
    So to keep all manipulation with web app inside the web app project, we
    use gulp.
  4. Proto definitions have imports with exact paths.
    e.g. 'common/repo/repo.proto'
    If we don't want to edit proto files, then we have to keep that structure.
    It's much easier to do it with gulp.
*/
gulp.task('protoc', () => {
  // Remove old functions (if they exist)
  gulp.src(protoPath)
    .pipe(clean())
    .on('data', () => {})
    .on('end', createFunctions);

  let protoImportList = [];
  function createFunctions() {
    // Create a proto folder
    fs.mkdirSync(protoPath);

    // Copy all packages to the proto folder
    function copyPackage(relativePath) {
      const absolutePath = path.join(
        startuposPath,
        relativePath
      );
      const filename = path.parse(absolutePath).base;
      protoImportList.push(filename);
      const newPackagePath = path.join(functionsPath, filename);
      fs.copySync(absolutePath, newPackagePath);

      return newPackagePath;
    }

    for (const protoPath of protoList) {
      const newProtoPath = copyPackage(protoPath);

      // Copy children of the proto to the proto folder
      const protoContent = fs.readFileSync(newProtoPath, 'utf8');
      const protoLines = protoContent.split('\n');
      for (let line of protoLines) {
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
          const newPackagePath = path.join(functionsPath, importPath);
          fs.copySync(packagePath, newPackagePath);
          protoImportList.push(importPath);
        }
      }
    }

    // Remove duplicate proto paths, in case of same proto was imported twice
    protoImportList = protoImportList.filter(
      (item, index) => protoImportList.indexOf(item) == index
    );

    // Create proto functions from the packages in the proto folder
    const protoImport = protoImportList.join(' ');
    exec(
      'protoc ' +
        `--proto_path=${functionsPath} ` +
        `--js_out=import_style=commonjs,binary:${functionsPath} ` +
        '--plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts ' +
        `--ts_out=${functionsPath} ` +
        protoImport,
      protocOnLoad
    );
  }

  // When protoc finished generating proto functions
  function protocOnLoad(error) {
    if (error) {
      throw error;
    }

    // Remove all *.proto files from the proto folder
    gulp.src(path.join(functionsPath, '**/*.proto'))
      .pipe(clean())
      .on('data', () => {})
      .on('end', removingOnLoad);
  }

  // When all *.proto files are removed
  function removingOnLoad() {
    // Create index file for proto functions
    let indexTS = '';
    for(let filepath of protoImportList) {
      const dir = path.parse(filepath).dir;
      const filename = path.parse(filepath).name + '_pb';
      const indexpath = path.join(dir, filename);
      indexTS += "export * from './functions/" + indexpath + "';\n";
    }
    fs.writeFileSync(path.join(protoPath, 'index.ts'), indexTS);

    onLoad();
  }

  function onLoad() {
    console.log('Proto functions are successfully created.');
  }
});
