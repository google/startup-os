import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { AppRoutes } from './app.routing';
import {
  AddUserDialogComponent,
  DashboardComponentList,
  DashboardServiceList,
  DeleteCommentDialogComponent,
  DeleteDiffDialogComponent,
  UserPopupComponent,
} from './dashboard';
import {
  FirebaseImports,
  FirebaseServices,
  MaterialImports,
} from './import';
import { LayoutComponentList } from './layout';
import {
  DirectiveList,
  PipeList,
  ServiceList,
} from './shared';

@NgModule({
  declarations: [
    AppComponent,
    ...DashboardComponentList,
    ...LayoutComponentList,
    ...PipeList,
    ...DirectiveList,
  ],
  imports: [
    HttpModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutes,
    ...MaterialImports,
    ...FirebaseImports,
  ],
  providers: [
    ...FirebaseServices,
    ...ServiceList,
    ...DashboardServiceList,
  ],
  entryComponents: [
    UserPopupComponent,
    AddUserDialogComponent,
    DeleteDiffDialogComponent,
    DeleteCommentDialogComponent,
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
