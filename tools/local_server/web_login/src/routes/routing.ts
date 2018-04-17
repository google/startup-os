import { RouterModule, Routes } from '@angular/router';

import { HomePageComponent } from './home/home.component';
import { Error404PageComponent } from './error404/error404.component';

const appRoutes: Routes = [
  { path: '', component: HomePageComponent },
  { path: '**', component: Error404PageComponent }
];

export const PageComponents = [
  HomePageComponent,
  Error404PageComponent
];

export const Router = RouterModule.forRoot(appRoutes, { useHash: false });
