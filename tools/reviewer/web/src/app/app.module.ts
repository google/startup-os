import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AngularFireModule } from 'angularfire2';
import { AngularFireAuth } from 'angularfire2/auth';
import { AppRoutes } from './app.routing';

import { AngularFireDatabase } from 'angularfire2/database';
import { config } from '../environments/firebase';
import { AppComponent } from './app.component';
import { DashboardModule } from './dashboard/dashboard.module';

import { AppComponents, Services } from './';

@NgModule({
  declarations: [AppComponent, ...AppComponents],
  imports: [
    AngularFireModule.initializeApp(config),
    BrowserModule,
    AppRoutes,
    DashboardModule
  ],
  providers: [AngularFireDatabase, ...Services, AngularFireAuth],
  bootstrap: [AppComponent]
})
export class AppModule {}
