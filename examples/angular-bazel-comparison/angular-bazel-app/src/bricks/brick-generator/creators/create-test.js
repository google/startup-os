function createTest(brickfile, brickCamel) {
  return `import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import { ${brickCamel}Component } from './${brickfile}.component';
import { ${brickCamel}ModuleNgSummary } from './${brickfile}.module.ngsummary';

// TODO(alexeagle): this helper should be in @angular/platform-browser-dynamic/testing
try {
  TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
} catch {
  // Ignore exceptions when calling it multiple times.
}

describe('BannerComponent (inline template)', () => {
  let component: ${brickCamel}Component;
  let fixture: ComponentFixture<${brickCamel}Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [${brickCamel}Component],  // declare the test component
      aotSummaries: ${brickCamel}ModuleNgSummary,
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(${brickCamel}Component);
    component = fixture.componentInstance;
  });

  it('should wait 1 sec', async(() => {
    setTimeout(() => {
      expect(component).toBeTruthy();
    }, 1000);
  }));
});
`;
}

module.exports = createTest;
