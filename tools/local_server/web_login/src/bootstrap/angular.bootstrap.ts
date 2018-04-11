import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { environment } from './environments/environment';
if (environment.production) {
  enableProdMode();
}

import { AngularModule } from './angular.module';
platformBrowserDynamic().bootstrapModule(AngularModule)
  .catch(err => console.log(err));
