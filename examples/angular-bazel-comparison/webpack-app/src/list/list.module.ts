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
import { Brick11Module } from '../bricks/brick-11/brick-11.module';
import { Brick12Module } from '../bricks/brick-12/brick-12.module';
import { Brick13Module } from '../bricks/brick-13/brick-13.module';
import { Brick14Module } from '../bricks/brick-14/brick-14.module';
import { Brick15Module } from '../bricks/brick-15/brick-15.module';
import { Brick16Module } from '../bricks/brick-16/brick-16.module';
import { Brick17Module } from '../bricks/brick-17/brick-17.module';
import { Brick18Module } from '../bricks/brick-18/brick-18.module';
import { Brick19Module } from '../bricks/brick-19/brick-19.module';
import { Brick20Module } from '../bricks/brick-20/brick-20.module';

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
    Brick11Module,
    Brick12Module,
    Brick13Module,
    Brick14Module,
    Brick15Module,
    Brick16Module,
    Brick17Module,
    Brick18Module,
    Brick19Module,
    Brick20Module,
  ],
  declarations: [ListComponent],
  exports: [ListComponent],
})
export class ListModule { }
