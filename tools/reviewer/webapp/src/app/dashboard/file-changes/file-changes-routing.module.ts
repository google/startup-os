import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FileChangesComponent } from './file-changes.component';

const routes: Routes = [
  { path: '', component: FileChangesComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FileChangesRoutingModule { }
