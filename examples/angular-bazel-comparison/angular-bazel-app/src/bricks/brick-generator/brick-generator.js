const fs = require('fs');
const path = require('path');

const createComponent = require('./creators/create-component');
const createHtml = require('./creators/create-html');
const createBuild = require('./creators/create-build');
const createModule = require('./creators/create-module');
const createTest = require('./creators/create-test');

for (let i = 1; i <= 10; i++) {
  const brickfile = 'brick-' + i;
  const brickCamel = 'Brick' + i;

  const newBrickPath = brickfile;
  fs.mkdirSync(newBrickPath);

  const componentName = brickfile + '.component.ts';
  fs.writeFileSync(path.join(newBrickPath, componentName), createComponent(brickfile, brickCamel));
  const htmlName = brickfile + '.component.html';
  fs.writeFileSync(path.join(newBrickPath, htmlName), createHtml(brickfile, brickCamel));
  const scssName = brickfile + '.component.scss';
  fs.writeFileSync(path.join(newBrickPath, scssName), '');
  const buildName = 'BUILD.bazel';
  fs.writeFileSync(path.join(newBrickPath, buildName), createBuild(brickfile, brickCamel));
  const moduleName = brickfile + '.module.ts';
  fs.writeFileSync(path.join(newBrickPath, moduleName), createModule(brickfile, brickCamel));
  const testName = brickfile + '.component.spec.ts';
  fs.writeFileSync(path.join(newBrickPath, testName), createTest(brickfile, brickCamel));
}
