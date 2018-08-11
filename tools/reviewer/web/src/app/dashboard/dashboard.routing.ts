import { RouterModule, Routes } from '@angular/router';

import {
  DiffComponent,
  DiffsComponent,
  FileChangesComponent,
} from './';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'diffs' },
      {
        path: 'diffs',
        component: DiffsComponent,
      },
      {
        path: 'diff/:id',
        component: DiffComponent,
      },
      {
        path: 'diff',
        children: [{ path: '**', component: FileChangesComponent }],
      },
    ],
  },
];

export const DashboardRoutes = RouterModule.forChild(routes);
