// ---------------------- Angular ----------------------
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';

// ---------------------- Firebase ----------------------
import { AngularFireModule } from 'angularfire2';
import { config } from './environments/firebase';
import { AngularFireAuth } from 'angularfire2/auth';

// ---------------------- Routing ----------------------
import { Router, PageComponents } from '@/routes/routing';

// ---------------------- Components ----------------------
import { AngularComponent } from '@/components';
import { Components } from '@/components/index.components.ts';

// ---------------------- Servies ----------------------
import { Services } from '@/services/index.services';

// ---------------------- NgModule ----------------------
@NgModule({
  declarations: [
    ...Components,
    ...PageComponents
  ],

  imports: [
    BrowserModule,
    HttpModule,
    AngularFireModule.initializeApp(config),
    Router
  ],

  providers: [
    AngularFireAuth,
    ...Services
  ],

  bootstrap: [AngularComponent]
})
export class AngularModule { }
