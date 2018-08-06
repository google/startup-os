import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { HelloWorldComponent } from './hello-world/hello-world.component';
import { HelloWorldModule } from './hello-world/hello-world.module';
import { DataInputComponent } from './data-input/data-input.component';
import { DataInputsModule } from './data-input/data-input.module';
import { ListComponent } from './list/list.component';
import { ListModule } from './list/list.module';
import { MockService } from './services/mock.service';

export const appRoutes: Routes = [
  { path: '', component: HelloWorldComponent },
  { path: 'data-input', component: DataInputComponent },
  { path: 'component-list', component: ListComponent },
];

@NgModule({
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes),
    HelloWorldModule,
    DataInputsModule,
    ListModule,
  ],
  declarations: [AppComponent],
  providers: [MockService],
  bootstrap: [AppComponent],
})
export class AppModule { }
