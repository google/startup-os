export * from './thread-comments.component';
export * from './thread-edit-comment';
export * from './comment-menu';

import { CommentMenuComponent } from './comment-menu';
import { ThreadCommentsComponent } from './thread-comments.component';
import { ThreadEditCommentComponent } from './thread-edit-comment';
export const ThreadCommentsComponentList = [
  ThreadCommentsComponent,
  ThreadEditCommentComponent,
  CommentMenuComponent,
];
