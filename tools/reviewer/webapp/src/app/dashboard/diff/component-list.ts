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
