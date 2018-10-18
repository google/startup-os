export * from './diff-discussion.component';
export * from './code-threads';
export * from './diff-threads';
export * from './thread';
export * from './discussion.service';
export * from './delete-comment-dialog';

// Components
import { CodeThreadsComponent } from './code-threads';
import { DeleteCommentDialogComponent } from './delete-comment-dialog';
import { DiffDiscussionComponent } from './diff-discussion.component';
import { DiffThreadsComponent } from './diff-threads';
import { ThreadComponent } from './thread';
export const DiffDiscussionComponentList = [
  DiffDiscussionComponent,
  CodeThreadsComponent,
  DiffThreadsComponent,
  ThreadComponent,
  DeleteCommentDialogComponent,
];

// Services
import { ThreadStateService } from './thread';
export const DiffDiscussionServiceList = [
  ThreadStateService,
];
