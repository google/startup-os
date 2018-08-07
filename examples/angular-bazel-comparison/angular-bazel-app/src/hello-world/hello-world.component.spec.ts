import { DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { HelloWorldComponent } from './hello-world.component';
import { MockService } from '../services/mock.service';
import { HelloWorldModuleNgSummary } from './hello-world.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('BannerComponent (inline template)', () => {
  let component: HelloWorldComponent;
  let fixture: ComponentFixture<HelloWorldComponent>;
  // let el: HTMLElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [HelloWorldComponent],  // declare the test component
      providers: [MockService],
      aotSummaries: HelloWorldModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HelloWorldComponent);
    component = fixture.componentInstance;
    // el = fixture.debugElement.query(By.css('div')).nativeElement;
  });

  it('should be created', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // it('should display original title', () => {
  //   fixture.detectChanges();
  //   expect(el.textContent).toContain(component.name);
  // });

  // it('should display a different test title', () => {
  //   component.name = 'Test';
  //   fixture.detectChanges();
  //   expect(el.textContent).toContain('Test');
  // });
});
