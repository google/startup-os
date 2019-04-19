import { TestBed } from '@angular/core/testing';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';

import { CoreModule } from '@/core';
import { SharedModule } from '@/shared';
import { AuthService, FirebaseService } from '@/core/services';
import { AuthMockService, FirebaseMockService } from '@/core/services/mock';
import { DiffModule } from './dashboard';

export function configureTestingModule(): void {
  TestBed.configureTestingModule({
    imports: [
      BrowserModule,
      BrowserAnimationsModule,
      CoreModule,
      SharedModule,
      RouterTestingModule,
      DiffModule,
    ],
    providers: [AuthMockService, FirebaseMockService],
  })
  .overrideProvider(AuthService, { useValue: new AuthMockService() })
  .overrideProvider(FirebaseService, { useValue: new FirebaseMockService() })
  .compileComponents();
}
