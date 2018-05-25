import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AngularFireModule } from 'angularfire2';
import { AngularFireAuth } from 'angularfire2/auth';
import { AppRoutes } from './app.routing';

import { AngularFireDatabase } from 'angularfire2/database';
import { config } from '../environments/firebase';
import { AppComponent } from './app.component';
import { LayoutModule } from './layout/layout.module';

import { AppComponents, Services } from './';

@NgModule({
  declarations: [AppComponent, ...AppComponents],
  imports: [
    AngularFireModule.initializeApp(config),
    AppRoutes,
    BrowserAnimationsModule,
    LayoutModule
  ],
  providers: [AngularFireDatabase, ...Services, AngularFireAuth],
  bootstrap: [AppComponent]
})
export class AppModule {}
