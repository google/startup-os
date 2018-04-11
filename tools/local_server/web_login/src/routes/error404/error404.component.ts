import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'error404-page',
  templateUrl: './error404.component.html'
})
export class Error404PageComponent {
  constructor(
    public title: Title
  ) {
    this.title.setTitle("Page doesn't exist");
  }
}
