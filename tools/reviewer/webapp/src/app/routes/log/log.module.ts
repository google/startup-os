import { NgModule } from '@angular/core';

import { SharedModule } from '@/shared';
import { LogRoutingModule } from './log-routing.module';
import { LogComponent } from './log.component';

@NgModule({
  imports: [
    SharedModule,
    LogRoutingModule,
  ],
  declarations: [LogComponent],
})
export class LogModule { }
