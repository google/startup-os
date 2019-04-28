import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { FirebaseConfig } from '@/core/proto';
import { GlobalRegistry } from '@/import';
import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

GlobalRegistry.getConfig().subscribe((firebaseConfig: FirebaseConfig) => {
  window['firebaseConfig'] = firebaseConfig.toObject();
  platformBrowserDynamic().bootstrapModule(AppModule);
});
