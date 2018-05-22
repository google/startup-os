import { RouterModule, Routes } from '@angular/router';
import { LoginComponent, PageNotFoundComponent } from './';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: 'diffs',
    loadChildren: 'app/layout/layout.module#LayoutModule'
  },
  { path: 'login', component: LoginComponent },
  { path: '**', component: PageNotFoundComponent }
];
export const AppRoutes = RouterModule.forRoot(routes, { useHash: false });
