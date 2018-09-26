export * from './file-changes.component';
export * from './code-changes';
export * from './commit-select';
export * from './services';

// Components
import { CodeChangesComponentList } from './code-changes';
import { CommitSelectComponent } from './commit-select';
import { FileChangesComponent } from './file-changes.component';
export const FileChangesComponentList = [
  FileChangesComponent,
  CommitSelectComponent,
  ...CodeChangesComponentList,
];

// Services
import { CodeChangesServiceList } from './code-changes';
import { StateService } from './services';
export const FileChangesServiceList = [
  StateService,
  ...CodeChangesServiceList,
];
