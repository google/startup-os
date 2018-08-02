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
  MatSnackBarModule,
  MatTableModule,
  MatToolbarModule,
} from '@angular/material';
import { RouterModule } from '@angular/router';

import { DirectiveList } from './directives';

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

@NgModule({
  imports: [RouterModule, ...SharedModules],
  exports: [SharedModules, ...DirectiveList],
  declarations: [...DirectiveList],
})
export class SharedModule { }
