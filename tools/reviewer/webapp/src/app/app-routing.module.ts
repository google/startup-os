import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '@/core';
import { LoginComponent, PageNotFoundComponent } from '@/routes';

const routes: Routes = [
  { path: '', redirectTo: 'diffs', pathMatch: 'full' },
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      { path: 'diffs', loadChildren: './routes/diffs/diffs.module#DiffsModule' },
      { path: 'diff/:id', loadChildren: './routes/diff/diff.module#DiffModule' },
      { path: 'log/:diffId/:repoId', loadChildren: './routes/log/log.module#LogModule' },
      {
        path: 'diff', children: [
          { path: '', component: PageNotFoundComponent },
          {
            path: '**',
            loadChildren: './routes/file-changes/file-changes.module#FileChangesModule',
          },
        ],
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
