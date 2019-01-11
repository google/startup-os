export * from './diff-header.component';
export * from './diff-header-titlebar';
export * from './diff-header-content';
export * from './user-popup';
export * from './reply-popup';
export * from './add-user-popup';
export * from './add-issue-popup';

// Components
import { AddIssuePopupComponent } from './add-issue-popup';
import { AddUserPopupComponent } from './add-user-popup';
import { DiffHeaderContentComponent } from './diff-header-content';
import { DiffHeaderTitlebarComponent } from './diff-header-titlebar';
import { DiffHeaderComponent } from './diff-header.component';
import { ReplyPopupComponent } from './reply-popup';
import { UserPopupComponent } from './user-popup';
export const DiffHeaderComponentList = [
  DiffHeaderComponent,
  DiffHeaderTitlebarComponent,
  DiffHeaderContentComponent,
  UserPopupComponent,
  ReplyPopupComponent,
  AddUserPopupComponent,
  AddIssuePopupComponent,
];
