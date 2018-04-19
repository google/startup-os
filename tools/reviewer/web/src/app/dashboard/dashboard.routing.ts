import { RouterModule, Routes } from '@angular/router';
import {
  DiffComponent,
  HomeComponent,
  ReviewComponent,
  ReviewsPanelComponent
} from './';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        component: HomeComponent
      },
      {
        path: 'diffs',
        component: ReviewsPanelComponent
      },
      {
        path: 'diff/:id',
        component: ReviewComponent
      },
      {
        path: 'diff',
        children: [{ path: '**', component: DiffComponent }]
      }
    ]
  }
];

export const DashboardRoutes = RouterModule.forChild(routes);
