export * from './home/home.component';
export * from './reviews-panel/';

import { HomeComponent } from './home/home.component';
import {
  AddCommentButtonsComponent,
  BugComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  CommentsComponent,
  DiffComponent,
  DiffService,
  LineNumbersComponent,
  PersonListComponent,
  ReviewComponent,
  ReviewsPanelComponent,
} from './reviews-panel/';

export const DashboardComponents = [
  BugComponent,
  DiffComponent,
  HomeComponent,
  ReviewComponent,
  ReviewsPanelComponent,
  PersonListComponent,
  CodeBlockComponent,
  CommentsComponent,
  LineNumbersComponent,
  AddCommentButtonsComponent,
  ChangesHighlightingComponent,
];

export const DashboardEntryComponents = [];

export const DashboardProviders = [
  DiffService
];
