export * from './thread.component';
export * from './thread-comment';
export * from './thread-reply';
export * from './thread-state.service';

import { ThreadCommentComponentList } from './thread-comment';
import { ThreadReplyComponent } from './thread-reply';
import { ThreadComponent } from './thread.component';
export const ThreadComponentComponentList = [
  ThreadComponent,
  ...ThreadCommentComponentList,
  ThreadReplyComponent,
];
