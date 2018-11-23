export * from './thread.component';
export * from './thread-comments';
export * from './thread-reply';
export * from './new-thread';
export * from './delete-comment-dialog';
export * from './thread.service';

import { DeleteCommentDialogComponent } from './delete-comment-dialog';
import { NewThreadComponent } from './new-thread';
import { ThreadCommentsComponentList } from './thread-comments';
import { ThreadReplyComponent } from './thread-reply';
import { ThreadComponent } from './thread.component';
export const ThreadComponentComponentList = [
  ThreadComponent,
  ...ThreadCommentsComponentList,
  ThreadReplyComponent,
  DeleteCommentDialogComponent,
  NewThreadComponent,
];
