export * from './diff.component';
export * from './diff-header';
export * from './diff-discussion';
export * from './diff-files';
export * from './delete-diff-dialog';

// Components
import { DeleteDiffDialogComponent } from './delete-diff-dialog';
import { DiffDiscussionComponentList } from './diff-discussion';
import { DiffFilesComponent } from './diff-files';
import { DiffHeaderComponentList } from './diff-header';
import { DiffComponent } from './diff.component';
export const DiffComponentList = [
  DiffComponent,
  ...DiffHeaderComponentList,
  DiffFilesComponent,
  ...DiffDiscussionComponentList,
  DeleteDiffDialogComponent,
];

// Services
import { DiffDiscussionServiceList } from './diff-discussion';
export const DiffServiceList = [
  ...DiffDiscussionServiceList,
];
