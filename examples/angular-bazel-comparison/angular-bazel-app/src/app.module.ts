import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { HelloWorldComponent } from './hello-world/hello-world.component';
import { HelloWorldModule } from './hello-world/hello-world.module';
import { MockService } from './services/mock.service';
import { DataInputComponent } from './data-input/data-input.component';
import { ListComponent } from './list/list.component';

export const appRoutes: Routes = [
  { path: '', component: HelloWorldComponent },
  { path: 'data-input', component: DataInputComponent },
  { path: 'component-list', component: ListComponent },
];

@NgModule({
  imports: [
    BrowserModule,
    RouterModule.forRoot(appRoutes),
  ],
  declarations: [AppComponent],
  bootstrap: [AppComponent],
  providers: [MockService],
})
export class AppModule {
}
