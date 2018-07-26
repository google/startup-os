export * from './reviews-panel/';

import {
  AddCommentButtonsComponent,
  CCListComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  CommentsComponent,
  EditablePropertyComponent,
  FileChangesComponent,
  FileChangesService,
  LineNumbersComponent,
  ReplyPopupComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewerListComponent,
  ReviewFilesComponent,
  ReviewService,
  ReviewsPanelComponent,
  ReviewTitlebarComponent,
} from './reviews-panel/';

export const DashboardComponents = [
  AddCommentButtonsComponent,
  CommentsComponent,
  ChangesHighlightingComponent,
  CodeBlockComponent,
  FileChangesComponent,
  EditablePropertyComponent,
  LineNumbersComponent,
  ReplyPopupComponent,
  ReviewerListComponent,
  CCListComponent,
  ReviewComponent,
  ReviewDiscussionComponent,
  ReviewFilesComponent,
  ReviewsPanelComponent,
  ReviewTitlebarComponent,
];

export const DashboardEntryComponents = [];

export const DashboardProviders = [FileChangesService, ReviewService];
