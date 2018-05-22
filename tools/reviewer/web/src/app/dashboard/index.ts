export * from './reviews-panel/';

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
