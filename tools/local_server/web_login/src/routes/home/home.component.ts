import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'home-page',
  templateUrl: './home.component.html'
})
export class HomePageComponent {
  constructor(
    public title: Title
  ) {
    this.title.setTitle('Log-in for StartupOS local server');
  }
}
