import { TestBed, async, ComponentFixture } from '@angular/core/testing';

import { Diff, BranchInfo, TextDiff, File, DiffLine, ChangeType } from '@/core/proto';
import { configureTestingModule } from '@/app-testing-module.spec';
import { CodeChangesComponent } from './code-changes.component';

describe('CodeChangesComponent', () => {
  let fixture: ComponentFixture<CodeChangesComponent>;
  let component: CodeChangesComponent;

  beforeEach(async(() => {
    configureTestingModule();

    fixture = TestBed.createComponent(CodeChangesComponent);
    component = fixture.debugElement.componentInstance;

    // Mock Data
    const diff = new Diff();
    const branchInfo = new BranchInfo();
    const textDiff = new TextDiff();
    const diffLine = new DiffLine();
    diffLine.setType(ChangeType.ADD);
    diffLine.setText('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
    textDiff.addRightDiffLine(diffLine);
    textDiff.setRightFileContents('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
    const file = new File();
    component.diff = diff;
    component.branchInfo = branchInfo;
    component.textDiff = textDiff;
    component.rightFile = file;
    component.leftFile = file;
    component.threads = [];
    fixture.detectChanges();
  }));

  it('can be created', () => {
    expect(component).toBeTruthy();
  });

  it('should contain code', () => {
    const pre: HTMLElement = fixture.nativeElement.querySelector(
      '.right-file .code pre',
    );
    expect(pre.textContent).toEqual('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
  });
});
