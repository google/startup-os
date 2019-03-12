const path = require('path');
const fs = require('fs-extra');
const { exec } = require('child_process');

// Since the script is located here: '/tools/protoc'
const repoRoot = path.resolve(__dirname, '../../');
// Absolute path of parent folder of the repo
const workspacePath = path.resolve(repoRoot, '../');

class Protoc {
  constructor() {
    const argPath = process.argv[2];

    // Path to project can be set by arguments or it's path, where the script is called from
    if (argPath) {
      this.projectPath = path.join(workspacePath, argPath);
    } else {
      this.projectPath = path.resolve('./');
    }

    const projectName = path.parse(this.projectPath).name;
    if (!fs.existsSync(this.projectPath)) {
      this.error(`Project "${projectName}" not found`);
    }

    this.projectProtoConfigPath = path.join(this.projectPath, 'proto');
    if (!fs.existsSync(this.projectProtoConfigPath + '.json')) {
      this.error('Project does not contain proto.json');
    }

    this.start();
  }

  start() {
    // List with names of .proto files. It's used in protoc.
    this.protocFilenameList = [];
    // List with absolute paths to each copied .proto file. To delete the files after use.
    this.protoFileList = [];

    // Load proto config of the project
    const projectProtoConfig = require(this.projectProtoConfigPath);
    if (!projectProtoConfig.exportPath) {
      this.error('Invalid proto.json. exportPath is not set');
    }
    this.projectExportPath = path.join(this.projectPath, projectProtoConfig.exportPath);
    this.projectFunctionsPath = path.join(this.projectExportPath, 'functions');

    // Clean proto folder
    fs.removeSync(this.projectExportPath);
    fs.mkdirSync(this.projectExportPath);

    // Copy project's .proto files to proto folder
    if (!projectProtoConfig.protoList || projectProtoConfig.protoList.length === 0) {
      this.error('proto.json does not contain any file');
    }
    for (const protoFile of projectProtoConfig.protoList) {
      this.copyPackage(path.join(workspacePath, protoFile));
    }

    // Create proto functions from the .proto files in the proto folder
    const protocImports = this.protocFilenameList.join(' ');
    exec(
      'protoc ' +
      `--proto_path=${this.projectFunctionsPath} ` +
      `--js_out=import_style=commonjs,binary:${this.projectFunctionsPath} ` +
      '--plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts ' +
      `--ts_out=${this.projectFunctionsPath} ` +
      protocImports, (error) => {
        this.removeProtoFiles(error);
      }
    );
  }

  // Copies a package to proto folder
  copyPackage(originProtoPath) {
    if (!fs.existsSync(originProtoPath)) {
      this.error(originProtoPath + ' not found');
    }
    const filename = path.parse(originProtoPath).base;
    const newPackagePath = path.join(this.projectFunctionsPath, filename);

    if (this.protoFileList.indexOf(newPackagePath) === -1) {
      // If the file wasn't copied before, then copy it now
      this.protocFilenameList.push(filename);
      this.protoFileList.push(newPackagePath);
      fs.copySync(originProtoPath, newPackagePath);
      this.importPackages(originProtoPath, newPackagePath);
    }
  }

  // Reads a file and copies its imports to proto folder
  importPackages(protoPath, newPackagePath) {
    const protoContent = fs.readFileSync(protoPath, 'utf8');
    let protoLines = protoContent.split('\n');
    let doesProtoContainImports = false;
    for (const lineNumber in protoLines) {
      const line = protoLines[lineNumber];
      // Get children paths from imports in code_review.proto
      if (line.startsWith('import')) {
        // Everything between quotes
        const regexExpression = /"(.*)"/;
        const importRelativePath = line.match(regexExpression)[1];

        if (importRelativePath.startsWith('google')) {
          // protoc is able to work with google imports by itself.
          // Unfortunately on unix system only.
          continue;
        }

        // Since all files are located in a same directory,
        // replace each relative import path to filename.
        // Example:
        // /path/to/my/file.proto -> file.proto
        doesProtoContainImports = true;
        const filename = path.parse(importRelativePath).base;
        protoLines[lineNumber] = `import "${filename}";`;

        const projectName = path.relative(workspacePath, protoPath).split('/')[0];
        const importPath = path.join(workspacePath, projectName, importRelativePath);
        this.copyPackage(importPath);
      }
    }
    if (doesProtoContainImports) {
      // No need to rewrite file, if it doesn't have imports
      fs.writeFileSync(newPackagePath, protoLines.join('\n'));
    }
  }

  // Removes all *.proto files from the proto folder
  removeProtoFiles(error) {
    if (error) {
      throw error;
    }
    for (const protoFile of this.protoFileList) {
      fs.removeSync(protoFile);
    }
    this.createIndexFile();
  }

  // Creates index file for proto functions
  createIndexFile() {
    let indexTS = '';
    for (let filename of this.protocFilenameList) {
      filename = path.parse(filename).name + '_pb';
      indexTS += "export * from './functions/" + filename + "';\n";
    }
    fs.writeFileSync(path.join(this.projectExportPath, 'index.ts'), indexTS);

    this.onLoad();
  }

  error(message) {
    console.log('\x1b[31mERROR:\x1b[0m ' + message);
    process.exit();
  }

  onLoad() {
    console.log('\x1b[32m%s\x1b[0m', 'Proto functions are successfully created');
  }
}
const protoc = new Protoc();
