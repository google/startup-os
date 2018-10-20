import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '@/shared';
import {
  DiffComponent,
  DiffsComponent,
  FileChangesComponent,
  LoginComponent,
  PageNotFoundComponent,
} from './dashboard';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: 'diffs', component: DiffsComponent },
      { path: 'diff/:id', component: DiffComponent },
      { path: 'diff', children: [{ path: '**', component: FileChangesComponent }] },
    ],
  },
  { path: 'login', component: LoginComponent },
  { path: '**', component: PageNotFoundComponent },
];

export const AppRoutes = RouterModule.forRoot(routes, { useHash: false });
