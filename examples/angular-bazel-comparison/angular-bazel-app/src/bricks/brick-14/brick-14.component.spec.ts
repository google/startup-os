import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { Brick14Component } from './brick-14.component';
import { Brick14ModuleNgSummary } from './brick-14.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('Brick14Component', () => {
  let component: Brick14Component;
  let fixture: ComponentFixture<Brick14Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Brick14Component],
      aotSummaries: Brick14ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Brick14Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
