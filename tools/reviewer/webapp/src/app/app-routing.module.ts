import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '@/core';
import { LoginComponent } from './login';
import { PageNotFoundComponent } from './page-not-found';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: 'diffs', loadChildren: './dashboard/diffs/diffs.module#DiffsModule' },
      { path: 'diff/:id', loadChildren: './dashboard/diff/diff.module#DiffModule' },
      {
        path: 'diff', children: [{
          path: '**',
          loadChildren: './dashboard/file-changes/file-changes.module#FileChangesModule',
        }],
      },
    ],
  },
  { path: 'login', component: LoginComponent },
  { path: '**', component: PageNotFoundComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    preloadingStrategy: PreloadAllModules,
  })],
  exports: [RouterModule],
})
export class AppRoutingModule { }
