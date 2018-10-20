export * from './diffs';
export * from './diff';
export * from './file-changes';
export * from './login';
export * from './page-not-found';

// Components
import { DiffComponentList } from './diff';
import { DiffsComponent } from './diffs';
import { FileChangesComponentList } from './file-changes';
import { LoginComponent } from './login';
import { PageNotFoundComponent } from './page-not-found';
export const DashboardComponentList = [
  DiffsComponent,
  ...DiffComponentList,
  ...FileChangesComponentList,
  LoginComponent,
  PageNotFoundComponent,
];

// Services
import { DiffServiceList } from './diff';
import { FileChangesServiceList } from './file-changes';
export const DashboardServiceList = [
  ...DiffServiceList,
  ...FileChangesServiceList,
];
