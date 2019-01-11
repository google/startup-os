import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '@/core';
import { DiffModuleFactory, DiffsModuleFactory, FileChangesModuleFactory } from '@/dashboard';
import { LoginComponent } from './login';
import { PageNotFoundComponent } from './page-not-found';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: 'diffs', loadChildren: DiffsModuleFactory },
      { path: 'diff/:id', loadChildren: DiffModuleFactory },
      {
        path: 'diff', children: [{
          path: '**',
          loadChildren: FileChangesModuleFactory,
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
