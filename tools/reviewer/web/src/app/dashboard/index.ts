export * from './home/home.component';
export * from './reviews-panel/';

import { HomeComponent } from './home/home.component';
import {
  CommentComponent,
  DiffComponent,
  ReviewComponent,
  ReviewsPanelComponent,
  ThreadComponent
} from './reviews-panel/';

export const DashboardComponents = [
  CommentComponent,
  DiffComponent,
  HomeComponent,
  ReviewComponent,
  ReviewsPanelComponent,
  ThreadComponent
];

export const DashboardEntryComponents = [];

export const DashboardProviders = [];
