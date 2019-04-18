import { TestBed, async, ComponentFixture } from '@angular/core/testing';

import { configureTestingModule } from '@/app-testing-module.spec';
import { Diff, Author } from '@/core/proto';
import { DiffHeaderComponent } from './diff-header.component';

describe('DiffHeaderComponent', () => {
  let fixture: ComponentFixture<DiffHeaderComponent>;
  let component: DiffHeaderComponent;

  beforeEach(async(() => {
    configureTestingModule();

    fixture = TestBed.createComponent(DiffHeaderComponent);
    component = fixture.debugElement.componentInstance;

    // Mock Data
    const diff = new Diff();
    diff.setId(100);
    const author = new Author();
    author.setEmail('testuser@gmail.com');
    author.setNeedsAttention(true);
    diff.setAuthor(author);
    diff.setDescription('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
    component.diff = diff;
    fixture.detectChanges();
  }));

  it('can be created', () => {
    expect(component).toBeTruthy();
  });

  it('should display user', () => {
    const pre: HTMLElement = fixture.nativeElement.querySelector('.titlebar .username');
    expect(pre.textContent).toEqual('testuser');
  });

  it('should display user', () => {
    const pre: HTMLElement = fixture.nativeElement.querySelector('.description .read-mode pre');
    expect(pre.textContent).toEqual('Lorem ipsum dolor sit amet, consectetur adipisicing elit.');
  });
});
