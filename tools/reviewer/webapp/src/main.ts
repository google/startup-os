import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { GlobalRegistry } from '@/import';
import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

GlobalRegistry.getConfig().subscribe(reviewerConfig => {
  platformBrowserDynamic([
    { provide: 'reviewerConfig', useValue: reviewerConfig.toObject() },
  ]).bootstrapModule(AppModule);
});
