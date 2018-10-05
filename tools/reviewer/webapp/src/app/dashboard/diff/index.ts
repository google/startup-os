export * from './diff.component';
export * from './diff-header';
export * from './diff-discussion';
export * from './diff-files';

// Components
import { DiffDiscussionComponentList } from './diff-discussion';
import { DiffFilesComponent } from './diff-files';
import { DiffHeaderComponentList } from './diff-header';
import { DiffComponent } from './diff.component';
export const DiffComponentList = [
  DiffComponent,
  ...DiffHeaderComponentList,
  DiffFilesComponent,
  ...DiffDiscussionComponentList,
];

// Services
import { DiffDiscussionServiceList } from './diff-discussion';
export const DiffServiceList = [
  ...DiffDiscussionServiceList,
];
