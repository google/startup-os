export * from './code-changes.component';
export * from './code-changes.interface';
export * from './comments';
export * from './services';

// Components
import { CodeChangesComponent } from './code-changes.component';
import { CommentsComponent } from './comments';
export const CodeChangesComponentList = [
  CodeChangesComponent,
  CommentsComponent,
];

// Services
import { CommentsService, LineService } from './services';
export const CodeChangesServiceList = [
  CommentsService,
  LineService,
];
