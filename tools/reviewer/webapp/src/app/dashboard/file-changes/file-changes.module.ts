import { NgModule } from '@angular/core';

import { SharedModule } from '@/shared';

import { CommitMenuComponentList } from './commit-menu';
import { FileChangesRoutingModule } from './file-changes-routing.module';
import { FileChangesComponent } from './file-changes.component';

@NgModule({
  imports: [
    SharedModule,
    FileChangesRoutingModule,
  ],
  declarations: [
    FileChangesComponent,
    ...CommitMenuComponentList,
  ],
})
export class FileChangesModule { }
export function FileChangesModuleFactory() {
  return FileChangesModule;
}
