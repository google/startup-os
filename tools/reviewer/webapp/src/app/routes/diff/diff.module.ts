import { NgModule } from '@angular/core';

import { SharedModule } from '@/shared';
import { DeleteDiffDialogComponent } from './delete-diff-dialog';
import { DiffDiscussionComponentList } from './diff-discussion';
import { DiffDiscussionServiceList } from './diff-discussion';
import { DiffFilesComponent } from './diff-files';
import {
  AddUserPopupComponent,
  DiffHeaderComponentList,
  UserPopupComponent,
} from './diff-header';
import { DiffRoutingModule } from './diff-routing.module';
import { DiffComponent } from './diff.component';

@NgModule({
  imports: [
    SharedModule,
    DiffRoutingModule,
  ],
  declarations: [
    DiffComponent,
    ...DiffHeaderComponentList,
    DiffFilesComponent,
    ...DiffDiscussionComponentList,
    DeleteDiffDialogComponent,
  ],
  providers: [
    ...DiffDiscussionServiceList,
  ],
  entryComponents: [
    DeleteDiffDialogComponent,
    UserPopupComponent,
    AddUserPopupComponent,
  ],
})
export class DiffModule { }
