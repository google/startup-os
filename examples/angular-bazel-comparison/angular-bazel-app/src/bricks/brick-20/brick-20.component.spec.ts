import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { Brick20Component } from './brick-20.component';
import { Brick20ModuleNgSummary } from './brick-20.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('Brick20Component', () => {
  let component: Brick20Component;
  let fixture: ComponentFixture<Brick20Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Brick20Component],
      aotSummaries: Brick20ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Brick20Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
