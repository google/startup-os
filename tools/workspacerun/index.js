#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const appName = process.argv[2];
if (!appName) {
  error('Please set app name');
}

// Find repo root by WORKSPACE file
const repoPath = findRepoRoot();
if (!repoPath) {
  error('Repo not found');
}

const workspacePath = path.resolve(repoPath, '../');
const workspacerunPath = path.join(workspacePath, 'startup-os/tools/workspacerun');

// Open app list and get path to the app
const apps = require(path.join(workspacerunPath, 'apps'));
let command;
for (const app of apps) {
  if (app.name === appName) {
    const appPath = path.join(workspacePath, app.path);
    command = 'node ' + appPath;
    break;
  }
}

if (command) {
  // Run app
  exec(command, (err, data) => {
    process.stdout.write(data);
  });
} else {
  error(`${appName} not found`);
}

function findRepoRoot() {
  let dirpath = path.resolve('./');
  for (; ;) {
    if (containsWorkspace(dirpath)) {
      return dirpath;
    } else {
      dirpath = path.resolve(dirpath, '../');
      if (dirpath === '/') {
        return;
      }
      continue;
    }
  }
}

function containsWorkspace(dirpath) {
  if (fs.lstatSync(dirpath).isDirectory()) {
    for (const item of fs.readdirSync(dirpath)) {
      if (item === 'WORKSPACE') {
        return true;
      }
    }
  }
  return false;
}

function error(message) {
  console.log('\x1b[31mERROR:\x1b[0m ' + message);
  process.exit();
}
