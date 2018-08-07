import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { Brick10Component } from './brick-10.component';
import { Brick10ModuleNgSummary } from './brick-10.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('Brick10Component', () => {
  let component: Brick10Component;
  let fixture: ComponentFixture<Brick10Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Brick10Component],
      aotSummaries: Brick10ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Brick10Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
