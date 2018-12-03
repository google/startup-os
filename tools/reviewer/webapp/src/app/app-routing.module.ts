import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '@/core';
import { LoginComponent } from './login';
import { PageNotFoundComponent } from './page-not-found';

import { DiffModule } from './dashboard/diff/diff.module';
import { DiffsModule } from './dashboard/diffs/diffs.module';
import { FileChangesModule } from './dashboard/file-changes/file-changes.module';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: 'diffs', loadChildren: () => DiffsModule },
      { path: 'diff/:id', loadChildren: () => DiffModule },
      {
        path: 'diff', children: [{
          path: '**',
          loadChildren: () => FileChangesModule,
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
