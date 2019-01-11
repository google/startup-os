import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiffsComponent } from './diffs.component';

const routes: Routes = [
  { path: '', component: DiffsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiffsRoutingModule { }
