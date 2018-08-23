function createComponent(brickfile, brickCamel) {
  return `import { Component, NgModule } from '@angular/core';

@Component({
  selector: 'app-${brickfile}',
  templateUrl: '${brickfile}.component.html',
  styleUrls: ['./${brickfile}.component.css']
})
export class ${brickCamel}Component { }
`;
}

module.exports = createComponent;
