import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  MatSnackBarModule,
  MatTableModule,
  MatToolbarModule,
} from '@angular/material';
import { RouterModule } from '@angular/router';

import { DirectiveList } from './directives';
import { PageLoadingComponent } from '@/page-loading';

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
];

const Declarations = [
  ...DirectiveList,
  PageLoadingComponent
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ...SharedModules
  ],
  exports: [
    SharedModules,
    ...Declarations
  ],
  declarations: [
    ...Declarations
  ],
})
export class SharedModule { }
