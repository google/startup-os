export * from './diff-discussion.component';
export * from './code-threads';
export * from './diff-threads';
export * from './thread';
export * from './discussion.service';

// Components
import { CodeThreadsComponent } from './code-threads';
import { DiffDiscussionComponent } from './diff-discussion.component';
import { DiffThreadsComponent } from './diff-threads';
import { ThreadComponent } from './thread';
export const DiffDiscussionComponentList = [
  DiffDiscussionComponent,
  CodeThreadsComponent,
  DiffThreadsComponent,
  ThreadComponent,
];

// Services
import { ThreadStateService } from './thread';
export const DiffDiscussionServiceList = [
  ThreadStateService,
];
