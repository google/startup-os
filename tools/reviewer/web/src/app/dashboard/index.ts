export * from './reviews-panel/';

import {
  AddCommentButtonsComponent,
  CCListComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  CommentsComponent,
  DiffComponent,
  DiffService,
  EditablePropertyComponent,
  LineNumbersComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewerListComponent,
  ReviewFilesComponent,
  ReviewsPanelComponent,
  ReviewTitlebarComponent,
} from './reviews-panel/';

export const DashboardComponents = [
  AddCommentButtonsComponent,
  CommentsComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  DiffComponent,
  EditablePropertyComponent,
  LineNumbersComponent,
  ReviewerListComponent,
  CCListComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewFilesComponent,
  ReviewsPanelComponent,
  ReviewTitlebarComponent,
];

export const DashboardEntryComponents = [];

export const DashboardProviders = [DiffService];
