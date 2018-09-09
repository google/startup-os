export * from './file-changes.component';
export * from './file-changes.service';
export * from './code-changes';

// Components
import { CodeChangesComponentList } from './code-changes';
import { FileChangesComponent } from './file-changes.component';
export const FileChangesComponentList = [
  FileChangesComponent,
  ...CodeChangesComponentList,
];

// Services
import { CodeChangesServiceList } from './code-changes';
import { FileChangesService } from './file-changes.service';
export const FileChangesServiceList = [
  FileChangesService,
  ...CodeChangesServiceList,
];
