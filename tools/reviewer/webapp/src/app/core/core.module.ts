import { NgModule } from '@angular/core';

import { ServiceList } from './services';

@NgModule({
  providers: [...ServiceList],
})
export class CoreModule { }
