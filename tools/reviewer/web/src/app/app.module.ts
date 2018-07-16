import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AngularFireModule } from 'angularfire2';
import { AngularFireAuth } from 'angularfire2/auth';
import { AngularFireAuthModule } from 'angularfire2/auth';
import { AngularFireDatabase } from 'angularfire2/database';
import { AngularFirestoreModule } from 'angularfire2/firestore';
import { config } from '../environments/firebase';
import { AppComponents, Services } from './';
import { AppComponent } from './app.component';
import { AppRoutes } from './app.routing';
import { LayoutModule } from './layout/layout.module';

@NgModule({
  declarations: [AppComponent, ...AppComponents],
  imports: [
    HttpModule,
    AngularFireModule.initializeApp(config),
    AngularFirestoreModule,
    AngularFireAuthModule,
    AppRoutes,
    BrowserAnimationsModule,
    LayoutModule,
  ],
  providers: [
    AngularFireDatabase,
    AngularFireAuth,
    ...Services,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
