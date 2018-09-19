import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import {
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatSelectModule,
  MatSnackBarModule,
  MatTableModule,
  MatToolbarModule,
} from '@angular/material';
import { RouterModule } from '@angular/router';

import { PageLoadingComponent } from '@/page-loading';
import { DirectiveList } from './directives';
import { PipeList } from './pipes';

const SharedModules = [
  FlexLayoutModule,
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatSnackBarModule,
  MatToolbarModule,
  MatTableModule,
  MatDialogModule,
  MatCheckboxModule,
  MatSelectModule,
];

const Declarations = [
  ...DirectiveList,
  ...PipeList,
  PageLoadingComponent,
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ...SharedModules,
  ],
  exports: [
    SharedModules,
    ...Declarations,
  ],
  declarations: [
    ...Declarations,
  ],
})
export class SharedModule { }
