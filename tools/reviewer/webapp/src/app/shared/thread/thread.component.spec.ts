import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { Diff, Thread, Comment } from '@/core/proto';
import { configureTestingModule } from '@/app-testing-module.spec';
import { ThreadComponent } from './thread.component';

describe('ThreadComponent', () => {
  let fixture: ComponentFixture<ThreadComponent>;
  let component: ThreadComponent;

  beforeEach(async(() => {
    configureTestingModule();

    fixture = TestBed.createComponent(ThreadComponent);
    component = fixture.debugElement.componentInstance;

    // Mock Data
    const diff = new Diff();
    const thread = new Thread();
    const comment = new Comment();
    comment.setContent('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
    comment.setCreatedBy('testuser@gmail.com');
    comment.setTimestamp(1555370405);
    thread.addComment(comment);
    thread.setType(Thread.Type.DIFF);
    diff.setDiffThreadList([thread]);
    component.diff = diff;
    component.thread = thread;
    fixture.detectChanges();
  }));

  it('can be created', () => {
    expect(component).toBeTruthy();
  });

  it('should contain a comment', () => {
    const div: HTMLElement = fixture.nativeElement.querySelector('.comment .header .username');
    expect(div.textContent).toEqual('testuser');

    const span: HTMLElement = fixture.nativeElement.querySelector(
      '.comment .content .read-mode span',
    );
    expect(span.textContent).toEqual('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
  });

  it('should expand a comment', () => {
    const debugElement: DebugElement = fixture.debugElement.query(
      By.css('.comment'),
    );
    debugElement.triggerEventHandler('click', null);
    fixture.detectChanges();
    const span: HTMLElement = fixture.nativeElement.querySelector(
      '.comment .header .time span',
    );
    expect(span.textContent).toEqual('3:02 AM, Jan 19');
  });

  it('should open reply', () => {
    const debugElement: DebugElement = fixture.debugElement.query(
      By.css('.comment .message .action-panel .comment-buttons .cr-thread-button'),
    );
    debugElement.triggerEventHandler('click', null);
    fixture.detectChanges();
    const textarea: HTMLElement = fixture.nativeElement.querySelector(
      '.comment .message textarea',
    );
    expect(textarea).toBeTruthy();
  });
});
