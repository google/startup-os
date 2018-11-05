import { NgModule } from '@angular/core';

import { SharedModule } from '@/shared';

import { DiffsRoutingModule } from './diffs-routing.module';
import { DiffsComponent } from './diffs.component';
import { SelectDashboardPopupComponent, SelectDashboardService } from './select-dashboard-popup';

@NgModule({
  imports: [
    SharedModule,
    DiffsRoutingModule,
  ],
  declarations: [
    DiffsComponent,
    SelectDashboardPopupComponent,
  ],
  providers: [SelectDashboardService],
  entryComponents: [SelectDashboardPopupComponent],
})
export class DiffsModule { }
