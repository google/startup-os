import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from '@/shared';
import { LayoutComponent, LayoutComponentList } from './';
import { LayoutRoutes } from './layout.routing';

@NgModule({
  imports: [CommonModule, SharedModule, LayoutRoutes],
  exports: [RouterModule, LayoutComponent],
  declarations: [...LayoutComponentList],
})
export class LayoutModule {}
