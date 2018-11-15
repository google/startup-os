export * from './thread.component';
export * from './thread-comments';
export * from './thread-reply';
export * from './thread-state.service';

import { ThreadCommentsComponentList } from './thread-comments';
import { ThreadReplyComponent } from './thread-reply';
import { ThreadComponent } from './thread.component';
export const ThreadComponentComponentList = [
  ThreadComponent,
  ...ThreadCommentsComponentList,
  ThreadReplyComponent,
];
