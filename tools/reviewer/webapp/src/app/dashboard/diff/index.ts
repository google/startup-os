export * from './diff.component';
export * from './diff-header';
export * from './diff-discussion';
export * from './diff-files';

// Components
import { DiffDiscussionComponent } from './diff-discussion';
import { DiffFilesComponent } from './diff-files';
import { DiffHeaderComponentList } from './diff-header';
import { DiffComponent } from './diff.component';
export const DiffComponentList = [
  DiffComponent,
  ...DiffHeaderComponentList,
  DiffFilesComponent,
  DiffDiscussionComponent,
];
