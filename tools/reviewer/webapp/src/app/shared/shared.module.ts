import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MaterialModule } from '@/import';
import { CodeChangesComponent } from './code-changes';
import { DiffStatusComponent } from './diff-status';
import { DirectiveList } from './directives';
import { PageLoadingComponent } from './page-loading';
import { PipeList } from './pipes';
import { SelectDashboardComponent } from './select-dashboard';
import { SpeechArrowComponent } from './speech-arrow';
import { DeleteCommentDialogComponent, ThreadComponentComponentList } from './thread';
import { ToolbarComponent } from './toolbar';

const Declarations = [
  DiffStatusComponent,
  PageLoadingComponent,
  ToolbarComponent,
  SpeechArrowComponent,
  SelectDashboardComponent,
  CodeChangesComponent,
  ...ThreadComponentComponentList,
  ...PipeList,
  ...DirectiveList,
];

const Modules = [
  FormsModule,
  ReactiveFormsModule,
  HttpClientModule,
  CommonModule,
  MaterialModule,
  FlexLayoutModule,
];

@NgModule({
  imports: [
    RouterModule,
    ...Modules,
  ],
  declarations: Declarations,
  exports: [
    ...Declarations,
    ...Modules,
  ],
  entryComponents: [DeleteCommentDialogComponent],
})
export class SharedModule { }
