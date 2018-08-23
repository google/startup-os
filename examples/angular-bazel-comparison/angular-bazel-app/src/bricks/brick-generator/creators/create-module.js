function createModule(brickfile, brickCamel) {
  return `import { NgModule } from '@angular/core';

import { ${brickCamel}Component } from './${brickfile}.component';

@NgModule({
  declarations: [${brickCamel}Component],
  exports: [${brickCamel}Component],
})
export class ${brickCamel}Module { }

`;
}

module.exports = createModule;
