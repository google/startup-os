import { NgModule } from '@angular/core';

import { SharedModule } from '@/shared';

import { CodeChangesComponentList } from './code-changes';
import { CodeChangesServiceList } from './code-changes';
import { CommitMenuComponentList } from './commit-menu';
import { FileChangesRoutingModule } from './file-changes-routing.module';
import { FileChangesComponent } from './file-changes.component';
import { StateService } from './services';

@NgModule({
  imports: [
    SharedModule,
    FileChangesRoutingModule,
  ],
  declarations: [
    FileChangesComponent,
    ...CommitMenuComponentList,
    ...CodeChangesComponentList,
  ],
  providers: [
    StateService,
    ...CodeChangesServiceList,
  ],
})
export class FileChangesModule { }
export function FileChangesModuleFactory() {
  return FileChangesModule;
}
