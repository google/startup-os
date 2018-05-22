import { SharedModule } from '@/shared';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LayoutComponent, LayoutComponents } from './';
import { LayoutRoutes } from './layout.routing';

@NgModule({
  imports: [CommonModule, SharedModule, LayoutRoutes],
  exports: [RouterModule, LayoutComponent],
  declarations: [...LayoutComponents]
})
export class LayoutModule {}
