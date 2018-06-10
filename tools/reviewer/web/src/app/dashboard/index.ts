export * from './reviews-panel/';

import {
  AddCommentButtonsComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  CommentsComponent,
  DiffComponent,
  DiffService,
  EditablePropertyComponent,
  LineNumbersComponent,
  PersonListComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewsPanelComponent,
  ReviewTitlebarComponent
} from './reviews-panel/';

export const DashboardComponents = [
  AddCommentButtonsComponent,
  CommentsComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  DiffComponent,
  EditablePropertyComponent,
  LineNumbersComponent,
  PersonListComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewsPanelComponent,
  ReviewTitlebarComponent
];

export const DashboardEntryComponents = [];

export const DashboardProviders = [DiffService];
