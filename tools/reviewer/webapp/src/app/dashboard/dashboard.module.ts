import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { SharedModule } from '@/shared';
import { DiffStatusComponent } from './';
import { DashboardRoutes } from './dashboard.routing';
import {
  AddUserDialogComponent,
  DeleteCommentDialogComponent,
  DeleteDiffDialogComponent,
  DiffComponentList,
  DiffDiscussionServiceList,
  UserPopupComponent,
} from './diff';
import { DiffsComponent } from './diffs';
import {
  FileChangesComponentList,
  FileChangesServiceList,
} from './file-changes';

@NgModule({
  imports: [
    CommonModule,
    DashboardRoutes,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
  ],
  exports: [RouterModule],
  declarations: [
    DiffsComponent,
    ...DiffComponentList,
    ...FileChangesComponentList,
    DiffStatusComponent,
  ],
  providers: [
    ...FileChangesServiceList,
    ...DiffDiscussionServiceList,
  ],
  entryComponents: [
    UserPopupComponent,
    AddUserDialogComponent,
    DeleteDiffDialogComponent,
    DeleteCommentDialogComponent,
  ],
})
export class DashboardModule { }
