import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { Brick18Component } from './brick-18.component';
import { Brick18ModuleNgSummary } from './brick-18.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('Brick18Component', () => {
  let component: Brick18Component;
  let fixture: ComponentFixture<Brick18Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Brick18Component],
      aotSummaries: Brick18ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Brick18Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
