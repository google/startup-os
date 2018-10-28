import { NgModule } from '@angular/core';

import { FirebaseModule } from '@/import';
import { ServiceList } from './services';

@NgModule({
  imports: [FirebaseModule],
  declarations: [],
  providers: [...ServiceList],
})
export class CoreModule { }
