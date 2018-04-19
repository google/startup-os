import { RouterModule, Routes } from '@angular/router';
import {
  HomeComponent,
  LoginComponent,
  PageNotFoundComponent,
  ReviewComponent,
  ReviewsPanelComponent
} from './';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadChildren: 'app/dashboard/dashboard.module#DashboardModule'
  },
  { path: 'login', component: LoginComponent },
  { path: '**', component: PageNotFoundComponent }
];
export const AppRoutes = RouterModule.forRoot(routes, { useHash: false });
