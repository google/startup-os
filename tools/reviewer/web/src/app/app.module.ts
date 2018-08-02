import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import {
  BrowserAnimationsModule,
} from '@angular/platform-browser/animations';

import { AngularFireModule } from 'angularfire2';
import { AngularFireAuthModule } from 'angularfire2/auth';
import { AngularFireAuth } from 'angularfire2/auth';
import { AngularFireDatabase } from 'angularfire2/database';
import { AngularFirestoreModule } from 'angularfire2/firestore';

import { config } from '../environments/firebase';
import { AppComponent } from './app.component';
import { AppRoutes } from './app.routing';
import { LayoutModule } from './layout/layout.module';
import { LoginComponent } from './login/login.component';
import { PageNotFoundComponent } from './page-not-found';
import { ServiceList } from './shared/services';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    PageNotFoundComponent,
  ],
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
    ...ServiceList,
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
