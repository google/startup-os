import { NgModule } from '@angular/core';

import { ListComponent } from './list.component';
import { Brick1Module } from '../bricks/brick-1/brick-1.module';
import { Brick2Module } from '../bricks/brick-2/brick-2.module';
import { Brick3Module } from '../bricks/brick-3/brick-3.module';
import { Brick4Module } from '../bricks/brick-4/brick-4.module';
import { Brick5Module } from '../bricks/brick-5/brick-5.module';
import { Brick6Module } from '../bricks/brick-6/brick-6.module';
import { Brick7Module } from '../bricks/brick-7/brick-7.module';
import { Brick8Module } from '../bricks/brick-8/brick-8.module';
import { Brick9Module } from '../bricks/brick-9/brick-9.module';
import { Brick10Module } from '../bricks/brick-10/brick-10.module';

@NgModule({
  imports: [
    Brick1Module,
    Brick2Module,
    Brick3Module,
    Brick4Module,
    Brick5Module,
    Brick6Module,
    Brick7Module,
    Brick8Module,
    Brick9Module,
    Brick10Module,
  ],
  declarations: [ListComponent],
  exports: [ListComponent],
})
export class ListModule { }
