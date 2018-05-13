import { RouterModule, Routes } from '@angular/router';
import { LoginComponent, PageNotFoundComponent } from './';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: 'diffs',
    loadChildren: 'app/dashboard/dashboard.module#DashboardModule'
  },
  { path: 'login', component: LoginComponent },
  { path: '**', component: PageNotFoundComponent }
];
export const AppRoutes = RouterModule.forRoot(routes, { useHash: false });
