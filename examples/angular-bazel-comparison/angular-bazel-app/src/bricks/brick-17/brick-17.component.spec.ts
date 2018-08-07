import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { Brick17Component } from './brick-17.component';
import { Brick17ModuleNgSummary } from './brick-17.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('Brick17Component', () => {
  let component: Brick17Component;
  let fixture: ComponentFixture<Brick17Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Brick17Component],
      aotSummaries: Brick17ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Brick17Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
