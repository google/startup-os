import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import 'hammerjs';

import { GlobalRegistry } from '@/import';
import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

GlobalRegistry.getConfig().subscribe(reviewerConfig => {
  window['reviewerConfig'] = reviewerConfig.toObject();
  platformBrowserDynamic().bootstrapModule(AppModule);
});
