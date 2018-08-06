import { Component, NgModule } from '@angular/core';

import { MockService } from '../services/mock.service';

@Component({
  selector: 'app-hello-world',
  templateUrl: 'hello-world.component.html',
  styleUrls: ['./hello-world.component.scss']
})
export class HelloWorldComponent {
  constructor(public mockService: MockService) { }
}
