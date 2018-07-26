import { RouterModule, Routes } from '@angular/router';
import {
  FileChangesComponent,
  ReviewComponent,
  ReviewsPanelComponent,
} from './';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'diffs' },
      {
        path: 'diffs',
        component: ReviewsPanelComponent,
      },
      {
        path: 'diff/:id',
        component: ReviewComponent,
      },
      {
        path: 'diff',
        children: [{ path: '**', component: FileChangesComponent }],
      },
    ],
  },
];

export const DashboardRoutes = RouterModule.forChild(routes);
