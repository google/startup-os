export * from './code-changes.component';
export * from './code-changes.interface';
export * from './services';

// Components
import { CodeChangesComponent } from './code-changes.component';
export const CodeChangesComponentList = [
  CodeChangesComponent,
];

// Services
import { CommentsService, LineService } from './services';
export const CodeChangesServiceList = [
  CommentsService,
  LineService,
];
