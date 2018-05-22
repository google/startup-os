import { Directives } from '@/shared';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatProgressSpinnerModule,
  MatSnackBarModule,
} from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { DashboardRoutes } from './dashboard.routing';

import {
  DashboardComponents,
  DashboardEntryComponents,
  DashboardProviders
} from './';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DashboardRoutes,
    FlexLayoutModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatFormFieldModule,
    BrowserAnimationsModule
  ],
  exports: [RouterModule],
  declarations: [
    ...DashboardComponents,
    Directives,
  ],
  entryComponents: DashboardEntryComponents,
  providers: DashboardProviders
})
export class DashboardModule {}

export function DashboardEntrypoint() {
  return DashboardModule;
}
